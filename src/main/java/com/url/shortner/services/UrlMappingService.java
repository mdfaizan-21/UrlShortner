package com.url.shortner.services;

import com.url.shortner.dtos.ClickEventDTO;
import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.ClickEvent;
import com.url.shortner.models.UrlMapping;
import com.url.shortner.models.User;
import com.url.shortner.repository.ClickEventRepository;
import com.url.shortner.repository.UrlMappingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UrlMappingService {

    private UrlMappingRepository urlMappingRepository;
    private ClickEventRepository clickEventRepository;

    public UrlMappingDTO convertToShortUrl(String originalUrl, User user) {
        String shortUrl=generateUrl();
        UrlMapping urlMapping=new UrlMapping();
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        urlMappingRepository.save(urlMapping);
        return convertToDTO(urlMapping);
    }

    private UrlMappingDTO convertToDTO(UrlMapping urlMapping) {
        UrlMappingDTO urlMappingDTO=new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        urlMappingDTO.setCreatedTime(urlMapping.getCreatedDate());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        return urlMappingDTO;
    }

    private String generateUrl (){
        String character="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random=new Random();
        StringBuilder url=new StringBuilder(8);
        for(int i=0;i<8;i++){
            url.append(character.charAt(random.nextInt(character.length())));
        }
        return url.toString();
    }

    public List<UrlMappingDTO> getAllUrlByUser(User user) {
        List<UrlMapping>urlMappings= urlMappingRepository.findByUser(user);
        List<UrlMappingDTO>urlMappingDTOS=new ArrayList<>();
        for(UrlMapping urlMapping:urlMappings){
            urlMappingDTOS.add(convertToDTO(urlMapping));
        }
        return urlMappingDTOS;
    }

    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        // 1. Look up the metadata for the short URL (to get its primary key/ID)
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);

        if (urlMapping != null) {
            // 2. Fetch all individual click records from the DB for this specific URL within the date range
            // Note: This returns a List<ClickEvent>, where each object is a single click.
            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, start, end)
                    .stream() // Convert list to a Stream for processing

                    // 3. Grouping Logic:
                    // Takes ClickEvent::getClickDate (LocalDateTime) -> converts to LocalDate (Year-Month-Day)
                    // Collectors.counting() counts how many clicks exist for each unique LocalDate
                    .collect(Collectors.groupingBy(
                            clickEvent -> clickEvent.getClickDate().toLocalDate(),
                            Collectors.counting()
                    ))

                    // 4. Mapping Logic:
                    // Convert the resulting Map<LocalDate, Long> back into a List of DTOs
                    .entrySet().stream()
                    .map(entry -> {
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey()); // The Date (e.g., 2023-10-27)
                        clickEventDTO.setCount(entry.getValue());   // The Total Clicks for that date
                        return clickEventDTO;
                    })
                    .collect(Collectors.toList()); // Terminal operation to return the final List
        }

        // Returns null if the shortUrl doesn't exist in the database
        return null;
    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        // 1. Fetch all UrlMapping objects created by this specific user
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);

        // 2. Query the ClickEvent table for any click associated with the list of URLs above.
        // .atStartOfDay() converts LocalDate to 00:00:00 time
        // .plusDays(1) ensures the end date is inclusive of the entire final day
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(
                urlMappings,
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay()
        );

        // 3. Flatten all clicks into a single stream and group by date
        return clickEvents.stream()
                .collect(Collectors.groupingBy(
                        click -> click.getClickDate().toLocalDate(), // Strip time to group by Day
                        Collectors.counting()                        // Aggregate count
                ));
    }

    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping=urlMappingRepository.findByShortUrl(shortUrl);
        if(urlMapping!=null){
            //update the click count for this short url
            urlMapping.setClickCount(urlMapping.getClickCount()+1);
            urlMappingRepository.save(urlMapping);

            //Record the click event
            ClickEvent clickEvent=new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now());
            clickEvent.setUrlMapping(urlMapping);
            clickEventRepository.save(clickEvent);
        }
        return urlMapping;
    }
}
