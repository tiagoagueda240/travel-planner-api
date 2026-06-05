package com.travelplanner.api.admin;

import com.travelplanner.api.models.User;
import com.travelplanner.api.repositories.TripRepository;
import com.travelplanner.api.repositories.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }

    @Transactional
    public void deleteUser(Integer id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    public AppStats getStats() {
        long totalUsers = userRepository.count();
        long totalTrips = tripRepository.count();
        return AppStats.builder()
                .totalUsers(totalUsers)
                .totalTrips(totalTrips)
                .build();
    }

    @Data
    @Builder
    public static class AppStats {
        private long totalUsers;
        private long totalTrips;
    }
}
