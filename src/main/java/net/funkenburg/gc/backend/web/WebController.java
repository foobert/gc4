package net.funkenburg.gc.backend.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.funkenburg.gc.backend.OngoingRequest;
import net.funkenburg.gc.backend.RequestQueue;
import net.funkenburg.gc.backend.geo.Coordinate;
import net.funkenburg.gc.backend.geo.Tile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;

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
        var center = extractLocation(requestForm);
        var tiles = Tile.near(center, requestForm.getDistance());
        var request = OngoingRequest.builder().tiles(tiles).build();
        requestQueue.enqueue(request);

        model.addAttribute("requestId", request.getId());
        model.addAttribute("status", request.getDetail());
        model.addAttribute("request", request);

        return "status";
    }

    @GetMapping("/mallorca")
    public String mallorca(Model model) {
        log.info("Request mallorca");

        var topLeft = Tile.fromCoordinates(new Coordinate(39.9880353137982, 2.2585241721318727));
        var bottomRight = Tile.fromCoordinates(new Coordinate(39.23773940219697, 3.5091854391187147));
        var tiles = new HashSet<Tile>();
        for (int x = topLeft.x(); x <= bottomRight.x(); x++) {
            for (int y = topLeft.y(); y <= bottomRight.y(); y++) {
                tiles.add(new Tile(x, y, topLeft.z()));
            }
        }

        var request = OngoingRequest.builder().tiles(tiles).build();
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

    @GetMapping(value = "/gpx", produces = "application/gpx")
    @ResponseBody
    public String getGpx(
            @RequestParam(name = "id") String id, @RequestParam(name = "type") String type) {
        Optional<OngoingRequest> lookup = requestQueue.lookup(id);
        if (lookup.isEmpty()) {
            throw new RuntimeException("boom");
        }
        var result = lookup.get().getResults().get(type);
        if (result == null) {
            throw new RuntimeException("boom");
        }
        return result.getGpx();
    }

    @GetMapping(value = "/gpi", produces = "application/gpi")
    @ResponseBody
    public byte[] getGpi(
            @RequestParam(name = "id") String id, @RequestParam(name = "type") String type) {
        Optional<OngoingRequest> lookup = requestQueue.lookup(id);
        if (lookup.isEmpty()) {
            throw new RuntimeException("boom");
        }
        var result = lookup.get().getResults().get(type);
        if (result == null) {
            throw new RuntimeException("boom");
        }
        return result.getGpi();
    }
}
