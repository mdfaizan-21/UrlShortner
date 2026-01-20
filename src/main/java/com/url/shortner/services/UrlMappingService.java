package com.url.shortner.services;

import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.UrlMapping;
import com.url.shortner.models.User;
import com.url.shortner.repository.UrlMappingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
public class UrlMappingService {

    private UrlMappingRepository urlMappingRepository;

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
}
