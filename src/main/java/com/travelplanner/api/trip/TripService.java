package com.travelplanner.api.trip;

import com.travelplanner.api.ai.GeminiItinerary;
import com.travelplanner.api.ai.GeminiService;
import com.travelplanner.api.exception.ResourceNotFoundException;
import com.travelplanner.api.models.Day;
import com.travelplanner.api.models.Place;
import com.travelplanner.api.models.Trip;
import com.travelplanner.api.models.User;
import com.travelplanner.api.repositories.DayRepository;
import com.travelplanner.api.repositories.PlaceRepository;
import com.travelplanner.api.repositories.TripRepository;
import com.travelplanner.api.routing.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final DayRepository dayRepository;
    private final PlaceRepository placeRepository;
    private final GeminiService geminiService;
    private final RoutingService routingService;

    @Transactional
    public Trip generateAndSave(GenerateTripRequest request, User user) {
        // 1. Ask Gemini to create the itinerary (respects the user's preferred language)
        GeminiItinerary itinerary = geminiService.generateItinerary(
                request.getDestination(),
                request.getTravelType(),
                request.getDurationDays(),
                user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "en"
        );

        // 2. Build the Trip entity
        Trip trip = Trip.builder()
                .user(user)
                .destination(request.getDestination())
                .durationDays(request.getDurationDays())
                .travelType(request.getTravelType())
                .days(new ArrayList<>())
                .build();

        // 3. Map each day from the AI response
        if (itinerary.getDays() != null) {
            itinerary.getDays().forEach(dayDto -> {
                AtomicInteger order = new AtomicInteger(0);
                List<Place> places = new ArrayList<>();

                if (dayDto.getPlaces() != null) {
                    dayDto.getPlaces().forEach(placeDto -> {
                        GeminiItinerary.GeminiPrice p = placeDto.getPrice();

                        List<String> photoUrls = placeDto.getPhotos() != null
                                ? placeDto.getPhotos().stream()
                                        .map(GeminiItinerary.GeminiPhoto::getUrl)
                                        .filter(url -> url != null && !url.isBlank())
                                        .collect(Collectors.toList())
                                : new ArrayList<>();

                        places.add(Place.builder()
                                .name(placeDto.getName())
                                .description(placeDto.getDescription())
                                .lat(placeDto.getLat())
                                .lng(placeDto.getLng())
                                .type(placeDto.getType())
                                .photoUrls(photoUrls)
                                .priceKids(p != null ? p.getKids() : 0)
                                .priceStudents(p != null ? p.getStudents() : 0)
                                .priceAdults(p != null ? p.getAdults() : 0)
                                .orderIndex(order.getAndIncrement())
                                .build());
                    });
                }

                // 4. Fetch road route for this day (best-effort)
                List<double[]> coords = places.stream()
                        .map(pl -> new double[]{pl.getLat(), pl.getLng()})
                        .collect(Collectors.toList());

                String routeGeoJson = null;
                try {
                    routeGeoJson = routingService.getRouteGeoJson(coords);
                } catch (Exception e) {
                    log.warn("Route unavailable for day {}: {}", dayDto.getDay(), e.getMessage());
                }

                Day day = Day.builder()
                        .trip(trip)
                        .dayNumber(dayDto.getDay())
                        .routeGeoJson(routeGeoJson)
                        .places(places)
                        .build();

                places.forEach(pl -> pl.setDay(day));
                trip.getDays().add(day);
            });
        }

        return tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public Page<Trip> getUserTrips(User user, Pageable pageable) {
        return tripRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Transactional(readOnly = true)
    public Trip getTripById(Long tripId, User user) {
        return tripRepository.findByIdAndUser(tripId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));
    }

    @Transactional
    public void deleteTrip(Long tripId, User user) {
        Trip trip = getTripById(tripId, user);
        tripRepository.delete(trip);
    }

    @Transactional
    public Trip updateTrip(Long tripId, User user, UpdateTripRequest request) {
        Trip trip = getTripById(tripId, user);
        if (request.getTitle() != null) {
            trip.setTitle(request.getTitle());
        }
        if (request.getNotes() != null) {
            trip.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            trip.setStatus(request.getStatus());
        }
        return tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public Day getDayById(Long tripId, Long dayId, User user) {
        getTripById(tripId, user); // ownership check
        return dayRepository.findByIdAndTripId(dayId, tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Day not found with id: " + dayId));
    }

    @Transactional
    public Place updatePlace(Long tripId, Long dayId, Long placeId, User user, UpdatePlaceRequest request) {
        getDayById(tripId, dayId, user); // ownership check
        Place place = placeRepository.findByIdAndDayId(placeId, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found with id: " + placeId));
        if (request.getVisited() != null) {
            place.setVisited(request.getVisited());
        }
        if (request.getPersonalNote() != null) {
            place.setPersonalNote(request.getPersonalNote());
        }
        return placeRepository.save(place);
    }
}
