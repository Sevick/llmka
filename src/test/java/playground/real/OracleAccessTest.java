package playground.real;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Disabled
@SpringBootTest(classes = {RestTemplate.class})
class OracleAccessTest {

    @Autowired
    RestTemplate restTemplate;

    @Test
    void testOracleAccess() {
        String testUrl = "https://blogs.oracle.com/java/rss";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/rss+xml");
        headers.add("User-Agent", "Postman1");

        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(testUrl, HttpMethod.GET, httpEntity, byte[].class);

        System.out.println(responseEntity);
    }
}
