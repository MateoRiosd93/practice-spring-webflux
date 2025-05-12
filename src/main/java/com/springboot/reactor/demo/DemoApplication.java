package com.springboot.reactor.demo;

import com.springboot.reactor.demo.models.Comentarios;
import com.springboot.reactor.demo.models.Usuario;
import com.springboot.reactor.demo.models.UsuarioComentarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
//		ejmeploIterable();
//		ejemploFlatMap();
//		ejemploToString();
//		ejemploColletList();
//		ejemploUsuarioComentariosFlapMap();
//		ejemploUsuarioComentariosZipWith();
//		ejemploUsuarioComentariosZipWithForma2();
		ejemploUsuarioComentariosZipWithRangos();
	}

	public void ejemploUsuarioComentariosZipWithRangos() {
		Flux.just(1,2,3,5)
				.map(number -> number * 2)
				.zipWith(Flux.range(0,4), (numeroFlujo1, numeroFlujo2) -> String.format("primer flux: %d, segundo flux: %d", numeroFlujo1, numeroFlujo2))
				.subscribe(log::info);
	}

		public void ejemploUsuarioComentariosZipWithForma2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			List<String> comentariosList = new ArrayList<>();
			comentariosList.add("Hola que mas");
			comentariosList.add("Esto es un comentario");
			comentariosList.add("Otro comentario");

			return new Comentarios(comentariosList);
		});

		Mono<UsuarioComentarios> usuarioComentariosMono =
				usuarioMono.zipWith(comentariosMono)
								.map(tupla -> {
									Usuario usuario = tupla.getT1();
									Comentarios comentarios = tupla.getT2();
									return new UsuarioComentarios(usuario, comentarios);
								});

		usuarioComentariosMono.subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));

	}

	public void ejemploUsuarioComentariosZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			List<String> comentariosList = new ArrayList<>();
			comentariosList.add("Hola que mas");
			comentariosList.add("Esto es un comentario");
			comentariosList.add("Otro comentario");

			return new Comentarios(comentariosList);
		});

//		Mono<UsuarioComentarios> usuarioComentariosMono =
//				usuarioMono.zipWith(comentariosMono, (usuario, comentarios) ->  new UsuarioComentarios(usuario, comentarios)));

		Mono<UsuarioComentarios> usuarioComentariosMono =
				usuarioMono.zipWith(comentariosMono, UsuarioComentarios::new);

		usuarioComentariosMono.subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));

	}

	public void ejemploUsuarioComentariosFlapMap(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			List<String> comentariosList = new ArrayList<>();
			comentariosList.add("Hola que mas");
			comentariosList.add("Esto es un comentario");
			comentariosList.add("Otro comentario");

			return new Comentarios(comentariosList);
		});

		Mono<UsuarioComentarios> usuarioComentariosMono =
				usuarioMono.flatMap(usuario -> comentariosMono.map(comentarios -> new UsuarioComentarios(usuario, comentarios)));

		usuarioComentariosMono.subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));

//		Mono<UsuarioComentarios> usuarioComentariosMono1 =
//				comentariosMono.flatMap(comentarios -> usuarioMono.map(usuario -> new UsuarioComentarios(usuario, comentarios)));
	}

	public void ejemploColletList() throws Exception {
		List<Usuario> nombresList = new ArrayList<>();
		nombresList.add( new Usuario("Mateo", "Rios"));
		nombresList.add( new Usuario("Maria", "Marin"));
		nombresList.add( new Usuario("Ivan", "Rios"));
		nombresList.add( new Usuario("Pedro", "Perez"));
		nombresList.add( new Usuario("Pepito", "Rios"));

		Flux.fromIterable(nombresList)
				.collectList()
				.subscribe(list -> {
					list.forEach(usuario -> log.info(usuario.toString()));
				});
	}

	public void ejemploToString() throws Exception {
		List<Usuario> nombresList = new ArrayList<>();
		nombresList.add( new Usuario("Mateo", "Rios"));
		nombresList.add( new Usuario("Maria", "Marin"));
		nombresList.add( new Usuario("Ivan", "Rios"));
		nombresList.add( new Usuario("Pedro", "Perez"));
		nombresList.add( new Usuario("Pepito", "Rios"));

		Flux.fromIterable(nombresList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("Rios".toUpperCase())){
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(String::toUpperCase)
				.subscribe(log::info);
	}


	public void ejemploFlatMap() throws Exception {
		List<String> nombresList = new ArrayList<>();
		nombresList.add("Mateo Rios");
		nombresList.add("Maria Marin");
		nombresList.add("Ivan Rios");
		nombresList.add("Pedro Perez");
		nombresList.add("Pepito Rios");

		Flux.fromIterable(nombresList)
				.map(nombreCompleto -> {
					String nombre = nombreCompleto.split(" ")[0];
					String apellido = nombreCompleto.split(" ")[1];

					return new Usuario(nombre.toUpperCase(), apellido);
				})
				.flatMap(usuario -> {
					if (usuario.getApellido().equalsIgnoreCase("Rios")){
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(usuario -> log.info(usuario.toString()));
	}

	public void ejmeploIterable() throws Exception {
		List<String> nombresList = new ArrayList<>();
		nombresList.add("Mateo Rios");
		nombresList.add("Maria Marin");
		nombresList.add("Ivan Rios");
		nombresList.add("Pedro Perez");
		nombresList.add("Pepito Rios");

//		Flux<String> nombres = Flux.just("Mateo Rios", "Maria Marin", "Ivan Rios", "Pedro Perez", "Pepito Rios");
		Flux<String> nombres = Flux.fromIterable(nombresList);

		Flux<Usuario> usuarios = nombres.filter(usuario -> usuario.split(" ")[1].toUpperCase().contains("RIOS"))
				.map(nombreCompleto -> {
					String nombre = nombreCompleto.split(" ")[0];
					String apellido = nombreCompleto.split(" ")[1];

					return new Usuario(nombre.toUpperCase(), apellido);
				})
				.doOnNext(usuario -> {
					if (usuario.getNombre().isEmpty()) {
						throw new RuntimeException("No se pueden nombres vacios");
					}

					System.out.println(usuario.getNombre() + " " + usuario.getApellido());
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});


		usuarios.subscribe(usuario -> log.info(usuario.toString()), error -> log.error(error.getMessage()), new Runnable() {
			@Override
			public void run() {
				log.info("Ha finalizado la ejecucion del observable con exito!");
			}
		});
	}
}
