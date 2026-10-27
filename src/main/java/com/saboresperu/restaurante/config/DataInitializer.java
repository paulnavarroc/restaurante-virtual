package com.saboresperu.restaurante.config;

import com.saboresperu.restaurante.entity.*;
import com.saboresperu.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RegionRepository regionRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (rolRepository.count() > 0) {
            log.info("Datos ya inicializados. Se omite el seed.");
            return;
        }

        log.info("Iniciando carga de datos de prueba...");

        // 1. Roles
        Rol rolCliente = crearRol("CLIENTE", "Cliente que realiza pedidos");
        Rol rolAdmin = crearRol("ADMINISTRADOR", "Administrador general");
        Rol rolChef = crearRol("CHEF", "Personal de cocina");
        Rol rolRepartidor = crearRol("REPARTIDOR", "Personal de delivery");

        // 2. Estados de pedido
        crearEstado("PENDIENTE", "Pedido registrado");
        crearEstado("CONFIRMADO", "Pedido confirmado");
        crearEstado("EN_PREPARACION", "En preparación en cocina");
        crearEstado("LISTO", "Listo para despacho");
        crearEstado("EN_CAMINO", "En camino al cliente");
        crearEstado("ENTREGADO", "Entregado al cliente");
        crearEstado("CANCELADO", "Pedido cancelado");

        // 3. Métodos de pago
        crearMetodoPago("EFECTIVO");
        crearMetodoPago("TARJETA");
        crearMetodoPago("YAPE");
        crearMetodoPago("PLIN");
        crearMetodoPago("TRANSFERENCIA");

        // 4. Regiones
        Region costa = crearRegion("COSTA", "Gastronomía de la costa peruana");
        Region sierra = crearRegion("SIERRA", "Gastronomía de la sierra peruana");
        Region selva = crearRegion("SELVA", "Gastronomía de la selva peruana");

        // 5. Categorías
        Categoria entradas = crearCategoria("ENTRADAS", "Entradas y piqueos");
        Categoria fondos = crearCategoria("PLATOS DE FONDO", "Platos principales");
        Categoria postres = crearCategoria("POSTRES", "Postres tradicionales");
        Categoria bebidas = crearCategoria("BEBIDAS", "Bebidas tradicionales");
        Categoria combos = crearCategoria("COMBOS", "Combos familiares");

        // 6. Productos
        crearProducto("Ceviche Clásico", "Pescado fresco marinado en limón con cebolla, ají limo, choclo y cancha.",
                new BigDecimal("32.00"), costa, entradas, 15,
                "https://images.unsplash.com/photo-1562707774-553917f561df?w=600");

        crearProducto("Lomo Saltado", "Lomo fino salteado al wok con tomate, cebolla y ají amarillo.",
                new BigDecimal("38.00"), costa, fondos, 20,
                "https://images.unsplash.com/photo-1535399831218-d5bd36d1a6b3?w=600");

        crearProducto("Ají de Gallina", "Pollo en cremosa salsa de ají amarillo con nueces y aceitunas.",
                new BigDecimal("28.00"), costa, fondos, 18,
                "https://images.unsplash.com/photo-1726514730572-7e0f664f60d2?w=600");

        crearProducto("Rocoto Relleno", "Rocoto arequipeño relleno de carne, pasas y queso gratinado.",
                new BigDecimal("26.00"), sierra, fondos, 25,
                "https://images.unsplash.com/photo-1654018334224-4887d9e85ffe?w=600");

        crearProducto("Chupe de Camarones", "Sopa espesa con camarones, papa, leche y ají amarillo.",
                new BigDecimal("42.00"), sierra, fondos, 22,
                "https://images.unsplash.com/photo-1533658266890-8bd362930725?w=600");

        crearProducto("Juane de Arroz", "Arroz con gallina envuelto en hoja de bijao.",
                new BigDecimal("30.00"), selva, fondos, 20,
                "https://images.unsplash.com/photo-1535399831218-d5bd36d1a6b3?w=600");

        crearProducto("Tacacho con Cecina", "Bolas de plátano verde con cecina ahumada.",
                new BigDecimal("28.00"), selva, entradas, 15,
                "https://images.unsplash.com/photo-1726514730572-7e0f664f60d2?w=600");

        crearProducto("Combo Familiar Costa", "Ceviche para 4 + 2 Lomos + bebidas.",
                new BigDecimal("120.00"), costa, combos, 30,
                "https://images.unsplash.com/photo-1562707774-553917f561df?w=600");

        // 7. Usuarios de prueba (password: 123456)
        String password = passwordEncoder.encode("123456");

        crearUsuario("Admin", "Sistema", "admin@saboresperu.com", password, Set.of(rolAdmin));
        crearUsuario("Carlos", "Cocina", "cocina@saboresperu.com", password, Set.of(rolChef));
        crearUsuario("Miguel", "Repartidor", "repartidor@saboresperu.com", password, Set.of(rolRepartidor));

        Usuario clienteUser = crearUsuario("Ana", "Cliente", "cliente@saboresperu.com", password, Set.of(rolCliente));
        Cliente cliente = Cliente.builder()
                .usuario(clienteUser)
                .documentoTipo("DNI")
                .documentoNumero("12345678")
                .fechaRegistro(LocalDateTime.now())
                .build();
        clienteRepository.save(cliente);

        log.info("Datos de prueba cargados correctamente.");
        log.info("Usuarios: admin@saboresperu.com / cocina@saboresperu.com / repartidor@saboresperu.com / cliente@saboresperu.com");
        log.info("Password de todos: 123456");
    }

    // ========== Helpers ==========

    private Rol crearRol(String nombre, String descripcion) {
        return rolRepository.save(Rol.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .activo(true)
                .build());
    }

    private void crearEstado(String nombre, String descripcion) {
        estadoPedidoRepository.save(EstadoPedido.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .build());
    }

    private void crearMetodoPago(String nombre) {
        metodoPagoRepository.save(MetodoPago.builder()
                .nombre(nombre)
                .activo(true)
                .build());
    }

    private Region crearRegion(String nombre, String descripcion) {
        return regionRepository.save(Region.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .activo(true)
                .build());
    }

    private Categoria crearCategoria(String nombre, String descripcion) {
        return categoriaRepository.save(Categoria.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .activo(true)
                .build());
    }

    private void crearProducto(String nombre, String descripcion, BigDecimal precio,
                               Region region, Categoria categoria, Integer tiempo, String imagen) {
        productoRepository.save(Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .region(region)
                .categoria(categoria)
                .tiempoPreparacion(tiempo)
                .imagenUrl(imagen)
                .disponible(true)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build());
    }

    private Usuario crearUsuario(String nombre, String apellido, String correo,
                                 String passwordHash, Set<Rol> roles) {
        return usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .correo(correo)
                .contrasenaHash(passwordHash)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .roles(roles)
                .build());
    }
}