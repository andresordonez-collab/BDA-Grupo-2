package cl.usach.bda.backend.viaje;

import cl.usach.bda.backend.repository.ViajeRepository;
import cl.usach.bda.backend.service.ViajeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViajeTransactionTest {
    @Configuration
    @EnableTransactionManagement
    @Import(ViajeService.class)
    static class Config {
        @Bean ViajeRepository repository() { return mock(ViajeRepository.class); }
        @Bean PlatformTransactionManager transactionManager() { return mock(PlatformTransactionManager.class); }
    }

    @Test
    void proxyAgrupaBloqueosYCallEnReadCommitted() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var repository = context.getBean(ViajeRepository.class);
            var manager = context.getBean(PlatformTransactionManager.class);
            var status = new SimpleTransactionStatus();
            when(manager.getTransaction(any())).thenReturn(status);
            when(repository.bloquearEstacion(2L)).thenReturn(true);
            when(repository.bloquearYObtenerPropietario(1L)).thenReturn(Optional.of(7L));
            context.getBean(ViajeService.class).finalizar(1L, 2L, 7L);
            var definition = ArgumentCaptor.forClass(TransactionDefinition.class);
            var order = inOrder(manager, repository);
            order.verify(manager).getTransaction(definition.capture());
            assertEquals(TransactionDefinition.ISOLATION_READ_COMMITTED, definition.getValue().getIsolationLevel());
            order.verify(repository).bloquearEstacion(2L);
            order.verify(repository).bloquearYObtenerPropietario(1L);
            order.verify(repository).finalizar(1L, 2L);
            order.verify(manager).commit(status);
            verify(manager, never()).rollback(any());
        }
    }

    @Test
    void viajeAjenoRevierteTransaccionSinCall() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var repository = context.getBean(ViajeRepository.class);
            var manager = context.getBean(PlatformTransactionManager.class);
            var status = new SimpleTransactionStatus();
            when(manager.getTransaction(any())).thenReturn(status);
            when(repository.bloquearEstacion(2L)).thenReturn(true);
            when(repository.bloquearYObtenerPropietario(1L)).thenReturn(Optional.of(8L));
            assertThrows(NoSuchElementException.class,
                    () -> context.getBean(ViajeService.class).finalizar(1L, 2L, 7L));
            verify(manager).rollback(status);
            verify(manager, never()).commit(any());
            verify(repository, never()).finalizar(any(), any());
        }
    }
}
