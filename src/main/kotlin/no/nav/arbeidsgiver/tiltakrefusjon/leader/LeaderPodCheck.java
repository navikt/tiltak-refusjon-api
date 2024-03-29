package no.nav.arbeidsgiver.tiltakrefusjon.leader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class LeaderPodCheck {

    private static final Logger log = LoggerFactory.getLogger(LeaderPodCheck.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String path;
    private final boolean enabled;

    public LeaderPodCheck(ObjectMapper objectMapper, @Value("${ELECTOR_PATH:}") String electorPath) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.enabled = isNotBlank(electorPath);
        this.path = "http://" + electorPath;
    }

    public boolean isLeaderPod() {
        if (!enabled) {
            return true;
        }
        String hostname;
        String leader;
        try {
            leader = getJSONFromUrl(path).getName();
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Feil v/henting av host for leader-election", e);
            return false;
        } catch (Exception e) {
            log.error("Feil v/oppslag i leader-elector", e);
            throw new RuntimeException(e);
        }
        return hostname.equals(leader);
    }

    private Elector getJSONFromUrl(String electorPath) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        var entity = new HttpEntity<>(headers);
        var responseEntity = restTemplate.exchange(electorPath, HttpMethod.GET, entity, String.class);
        return objectMapper.readValue((String) responseEntity.getBody(), Elector.class);
    }

    private static class Elector {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
