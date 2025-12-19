package ru.practicum.stats.client;

import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatsClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.restTemplate = new RestTemplate();
    }

    public void hit(HitDto hitDto) {
        restTemplate.postForEntity(serverUrl + "/hit", hitDto, Void.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", start.format(DATE_TIME_FORMATTER));
        params.put("end", end.format(DATE_TIME_FORMATTER));
        params.put("uris", uris);
        params.put("unique", unique);

        ViewStatsDto[] response = restTemplate.getForObject(serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}", ViewStatsDto[].class, params);

        return response == null ? List.of() : Arrays.asList(response);
    }
}