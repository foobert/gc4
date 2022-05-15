package net.funkenburg.gc.backend.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.OngoingRequest;
import net.funkenburg.gc.backend.RequestQueue;
import net.funkenburg.gc.backend.geo.Coordinate;
import net.funkenburg.gc.backend.geo.Tile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.Set;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WebController {
    private final RequestQueue requestQueue;

    @GetMapping("/request")
    public String request(
            @RequestParam(name = "name", required = false, defaultValue = "World") String name,
            Model model) {
        model.addAttribute("name", name);
        model.addAttribute("requestForm", new RequestForm());
        return "request";
    }

    @PostMapping("/request")
    public String postRequest(@ModelAttribute RequestForm requestForm, Model model) {
        log.info("Request {}", requestForm.getLocation());
        //        var fake = new Coordinate(51.33702374516727, 12.372297128375628);
        var fake = extractLocation(requestForm);
        var tile = Tile.fromCoordinates(fake);
        var request = OngoingRequest.builder().tiles(Set.of(tile)).build();
        requestQueue.enqueue(request);

        model.addAttribute("requestId", request.getId());
        model.addAttribute("status", request.getDetail());
        model.addAttribute("request", request);

        return "status";
    }

    private Coordinate extractLocation(RequestForm form) {
        String[] parts = form.getLocation().split(" ", 2);
        double lat = Double.parseDouble(parts[0]);
        double lon = Double.parseDouble(parts[1]);
        return new Coordinate(lat, lon);
    }

    @GetMapping("/status")
    public String getStatus(@RequestParam(name = "id") String id, Model model) {
        Optional<OngoingRequest> lookup = requestQueue.lookup(id);
        if (lookup.isEmpty()) {
            model.addAttribute("requests", requestQueue.getIds());
            return "list";
        }
        model.addAttribute("requestId", id);
        model.addAttribute("status", lookup.map(OngoingRequest::getDetail).orElse("404"));
        model.addAttribute("request", lookup.get());
        return "status";
    }

    @GetMapping(value = "/gpi", produces = "application/gpi")
    @ResponseBody
    public byte[] getGpi(
            @RequestParam(name = "id") String id, @RequestParam(name = "type") String type) {
        Optional<OngoingRequest> lookup = requestQueue.lookup(id);
        if (lookup.isEmpty()) {
            throw new RuntimeException("boom");
        }
        byte[] gpi = lookup.get().getResult().get(type);
        if (gpi == null) {
            throw new RuntimeException("boom");
        }
        return gpi;
    }
}
