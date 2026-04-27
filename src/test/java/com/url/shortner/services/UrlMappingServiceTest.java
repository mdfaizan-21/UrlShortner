package com.url.shortner.services;

import com.url.shortner.dtos.ClickEventDTO;
import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.ClickEvent;
import com.url.shortner.models.UrlMapping;
import com.url.shortner.models.User;
import com.url.shortner.repository.ClickEventRepository;
import com.url.shortner.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UrlMappingService Tests")
class UrlMappingServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;
    @Mock
    private ClickEventRepository clickEventRepository;
    @InjectMocks
    private UrlMappingService urlMappingService;

    private User testUser;
    private UrlMapping testUrlMapping;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testUrlMapping = new UrlMapping();
        testUrlMapping.setId(1L);
        testUrlMapping.setOriginalUrl("https://www.example.com/long");
        testUrlMapping.setShortUrl("aBcD1234");
        testUrlMapping.setUser(testUser);
        testUrlMapping.setCreatedDate(LocalDateTime.of(2026, 4, 27, 10, 0));
        testUrlMapping.setClickCount(5);
    }

    private UrlMapping makeMapping(Long id, String orig, String shortU, User u, int clicks) {
        UrlMapping m = new UrlMapping();
        m.setId(id); m.setOriginalUrl(orig); m.setShortUrl(shortU);
        m.setUser(u); m.setClickCount(clicks);
        m.setCreatedDate(LocalDateTime.now());
        return m;
    }

    private ClickEvent makeClick(Long id, LocalDateTime date, UrlMapping mapping) {
        ClickEvent c = new ClickEvent();
        c.setId(id); c.setClickDate(date); c.setUrlMapping(mapping);
        return c;
    }

    // --- convertToShortUrl ---
    @Nested @DisplayName("convertToShortUrl()")
    class ConvertToShortUrl {
        @Test @DisplayName("should save mapping and return valid DTO")
        void savesAndReturnsDto() {
            when(urlMappingRepository.save(any(UrlMapping.class))).thenAnswer(i -> { ((UrlMapping)i.getArgument(0)).setId(1L); return i.getArgument(0); });
            UrlMappingDTO dto = urlMappingService.convertToShortUrl("https://example.com", testUser);
            assertNotNull(dto);
            assertEquals("https://example.com", dto.getOriginalUrl());
            assertEquals("testuser", dto.getUsername());
            assertEquals(8, dto.getShortUrl().length());
            assertTrue(dto.getShortUrl().matches("[A-Za-z0-9]{8}"));
            verify(urlMappingRepository).save(any(UrlMapping.class));
        }

        @Test @DisplayName("should set creation date before saving")
        void setsCreatedDate() {
            ArgumentCaptor<UrlMapping> cap = ArgumentCaptor.forClass(UrlMapping.class);
            when(urlMappingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            urlMappingService.convertToShortUrl("https://example.com", testUser);
            verify(urlMappingRepository).save(cap.capture());
            assertNotNull(cap.getValue().getCreatedDate());
        }

        @Test @DisplayName("should associate mapping with correct user")
        void associatesUser() {
            ArgumentCaptor<UrlMapping> cap = ArgumentCaptor.forClass(UrlMapping.class);
            when(urlMappingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            urlMappingService.convertToShortUrl("https://example.com", testUser);
            verify(urlMappingRepository).save(cap.capture());
            assertEquals(testUser, cap.getValue().getUser());
        }
    }

    // --- getAllUrlByUser ---
    @Nested @DisplayName("getAllUrlByUser()")
    class GetAllUrlByUser {
        @Test @DisplayName("should return DTOs for all user mappings")
        void returnsAllDtos() {
            UrlMapping m1 = makeMapping(1L, "https://a.com", "s001", testUser, 3);
            UrlMapping m2 = makeMapping(2L, "https://b.com", "s002", testUser, 7);
            when(urlMappingRepository.findByUser(testUser)).thenReturn(List.of(m1, m2));
            List<UrlMappingDTO> results = urlMappingService.getAllUrlByUser(testUser);
            assertEquals(2, results.size());
            assertEquals("https://a.com", results.get(0).getOriginalUrl());
            assertEquals(7, results.get(1).getClickCount());
        }

        @Test @DisplayName("should return empty list when no mappings")
        void returnsEmpty() {
            when(urlMappingRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
            assertTrue(urlMappingService.getAllUrlByUser(testUser).isEmpty());
        }
    }

    // --- getClickEventsByDate ---
    @Nested @DisplayName("getClickEventsByDate()")
    class GetClickEventsByDate {
        @Test @DisplayName("should group clicks by date")
        void groupsByDate() {
            LocalDateTime s = LocalDateTime.of(2026,4,1,0,0), e = LocalDateTime.of(2026,4,30,23,59);
            when(urlMappingRepository.findByShortUrl("aBcD1234")).thenReturn(testUrlMapping);
            when(clickEventRepository.findByUrlMappingAndClickDateBetween(testUrlMapping, s, e))
                .thenReturn(List.of(
                    makeClick(1L, LocalDateTime.of(2026,4,10,14,0), testUrlMapping),
                    makeClick(2L, LocalDateTime.of(2026,4,10,16,0), testUrlMapping),
                    makeClick(3L, LocalDateTime.of(2026,4,15,9,0), testUrlMapping)));
            List<ClickEventDTO> res = urlMappingService.getClickEventsByDate("aBcD1234", s, e);
            assertEquals(2, res.size());
            Map<LocalDate,Long> map = res.stream().collect(Collectors.toMap(ClickEventDTO::getClickDate, ClickEventDTO::getCount));
            assertEquals(2L, map.get(LocalDate.of(2026,4,10)));
            assertEquals(1L, map.get(LocalDate.of(2026,4,15)));
        }

        @Test @DisplayName("should return null when short URL not found")
        void returnsNull() {
            when(urlMappingRepository.findByShortUrl("bad")).thenReturn(null);
            assertNull(urlMappingService.getClickEventsByDate("bad", LocalDateTime.now(), LocalDateTime.now()));
            verify(clickEventRepository, never()).findByUrlMappingAndClickDateBetween(any(), any(), any());
        }

        @Test @DisplayName("should return empty list when no clicks in range")
        void returnsEmpty() {
            LocalDateTime s = LocalDateTime.of(2026,4,1,0,0), e = LocalDateTime.of(2026,4,30,23,59);
            when(urlMappingRepository.findByShortUrl("aBcD1234")).thenReturn(testUrlMapping);
            when(clickEventRepository.findByUrlMappingAndClickDateBetween(testUrlMapping, s, e)).thenReturn(Collections.emptyList());
            assertTrue(urlMappingService.getClickEventsByDate("aBcD1234", s, e).isEmpty());
        }
    }

    // --- getTotalClicksByUserAndDate ---
    @Nested @DisplayName("getTotalClicksByUserAndDate()")
    class GetTotalClicksByUserAndDate {
        @Test @DisplayName("should aggregate clicks across all user URLs by date")
        void aggregatesClicks() {
            LocalDate s = LocalDate.of(2026,4,1), e = LocalDate.of(2026,4,30);
            UrlMapping m1 = makeMapping(1L,"a","s1",testUser,0);
            UrlMapping m2 = makeMapping(2L,"b","s2",testUser,0);
            List<UrlMapping> maps = List.of(m1,m2);
            when(urlMappingRepository.findByUser(testUser)).thenReturn(maps);
            when(clickEventRepository.findByUrlMappingInAndClickDateBetween(maps, s.atStartOfDay(), e.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(
                    makeClick(1L, LocalDateTime.of(2026,4,5,10,0), m1),
                    makeClick(2L, LocalDateTime.of(2026,4,5,14,0), m2),
                    makeClick(3L, LocalDateTime.of(2026,4,20,9,0), m1)));
            Map<LocalDate,Long> res = urlMappingService.getTotalClicksByUserAndDate(testUser, s, e);
            assertEquals(2, res.size());
            assertEquals(2L, res.get(LocalDate.of(2026,4,5)));
            assertEquals(1L, res.get(LocalDate.of(2026,4,20)));
        }

        @Test @DisplayName("should return empty map when no clicks")
        void emptyMap() {
            LocalDate s = LocalDate.of(2026,4,1), e = LocalDate.of(2026,4,30);
            when(urlMappingRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
            when(clickEventRepository.findByUrlMappingInAndClickDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());
            assertTrue(urlMappingService.getTotalClicksByUserAndDate(testUser, s, e).isEmpty());
        }
    }

    // --- getOriginalUrl ---
    @Nested @DisplayName("getOriginalUrl()")
    class GetOriginalUrl {
        @Test @DisplayName("should return mapping and increment click count")
        void incrementsCount() {
            testUrlMapping.setClickCount(5);
            when(urlMappingRepository.findByShortUrl("aBcD1234")).thenReturn(testUrlMapping);
            when(urlMappingRepository.save(any())).thenReturn(testUrlMapping);
            UrlMapping res = urlMappingService.getOriginalUrl("aBcD1234");
            assertNotNull(res);
            assertEquals(6, res.getClickCount());
            verify(urlMappingRepository).save(testUrlMapping);
        }

        @Test @DisplayName("should record a click event")
        void recordsClickEvent() {
            when(urlMappingRepository.findByShortUrl("aBcD1234")).thenReturn(testUrlMapping);
            when(urlMappingRepository.save(any())).thenReturn(testUrlMapping);
            ArgumentCaptor<ClickEvent> cap = ArgumentCaptor.forClass(ClickEvent.class);
            urlMappingService.getOriginalUrl("aBcD1234");
            verify(clickEventRepository).save(cap.capture());
            assertNotNull(cap.getValue().getClickDate());
            assertEquals(testUrlMapping, cap.getValue().getUrlMapping());
        }

        @Test @DisplayName("should return null and not save when short URL not found")
        void returnsNullIfNotFound() {
            when(urlMappingRepository.findByShortUrl("bad")).thenReturn(null);
            assertNull(urlMappingService.getOriginalUrl("bad"));
            verify(urlMappingRepository, never()).save(any());
            verify(clickEventRepository, never()).save(any());
        }

        @Test @DisplayName("should increment from zero")
        void incrementsFromZero() {
            testUrlMapping.setClickCount(0);
            when(urlMappingRepository.findByShortUrl("aBcD1234")).thenReturn(testUrlMapping);
            when(urlMappingRepository.save(any())).thenReturn(testUrlMapping);
            assertEquals(1, urlMappingService.getOriginalUrl("aBcD1234").getClickCount());
        }
    }
}
