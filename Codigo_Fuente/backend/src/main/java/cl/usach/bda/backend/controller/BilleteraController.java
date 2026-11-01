package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.entities.BilleteraEntity;
import cl.usach.bda.backend.service.BilleteraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billeteras")
public class BilleteraController {

    private final BilleteraService billeteraService;

    public BilleteraController(BilleteraService billeteraService) {
        this.billeteraService = billeteraService;
    }

    @GetMapping
    public ResponseEntity<List<BilleteraEntity>> listarBilleteras() {
        return ResponseEntity.ok(billeteraService.listarBilleteras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BilleteraEntity> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(billeteraService.obtenerBilleteraPorId(id));
    }

    @PostMapping
    public ResponseEntity<BilleteraEntity> crear(@RequestBody BilleteraEntity billetera) {
        BilleteraEntity creado = billeteraService.guardarBilletera(billetera);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BilleteraEntity> actualizar(
            @PathVariable("id") Long id,
            @RequestBody BilleteraEntity billetera) {
        return ResponseEntity.ok(billeteraService.actualizarBilletera(id, billetera));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        billeteraService.eliminarBilletera(id);
        return ResponseEntity.noContent().build();
    }
}
