package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

    @RequestMapping("/")
    public String home() {
        return "Hello Docker World";
    }

    @RequestMapping(value="/get", method=RequestMethod.GET)
    public String get(@RequestParam String key) {
        return key;
    }

    @RequestMapping(value="/put", method= RequestMethod.POST)
    public String put(@RequestParam String key, @RequestParam String value) {
        return key+value;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}