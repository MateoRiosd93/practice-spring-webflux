package com.springboot.reactor.demo;

import com.springboot.reactor.demo.models.Comentarios;
import com.springboot.reactor.demo.models.Usuario;
import com.springboot.reactor.demo.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Time;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

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
//		ejemploUsuarioComentariosZipWithRangos();
//		ejemploInterval();
//		ejemploDelayElements();
//		ejemploIntervaloInfinito();
//		ejemploIntervalDesdeCreate();
		ejemploContraPresion();
	}

	public void ejemploContraPresion(){
		Flux.range(1, 10)
				.log()
				.limitRate(5)
				.subscribe();
//				NOTA: Esta es la manera de hacerlo utilizando sobrecarga de metodos implementando el Subscriber()
//				.subscribe(new Subscriber<Integer>() {
//					Subscription subscription;
//					private Integer limit = 5;
//					private Integer consumido = 0;
//
//					@Override
//					public void onSubscribe(Subscription subscription) {
//						this.subscription = subscription;
//						this.subscription.request(limit);
//					}
//
//					@Override
//					public void onNext(Integer integer) {
//						log.info(integer.toString());
//						consumido++;
//						if (consumido.equals(limit)) {
//							consumido = 0;
//							this.subscription.request(limit);
//						}
//					}
//
//					@Override
//					public void onError(Throwable throwable) {
//
//					}
//
//					@Override
//					public void onComplete() {
//
//					}
//				});
	}

	public void ejemploIntervalDesdeCreate(){
		Flux.create(emitter -> {
					Timer time = new Timer();
					time.schedule(new TimerTask() {
						private Integer contador = 0;

						@Override
						public void run() {
							emitter.next(++contador);
							if (contador == 10) {
								time.cancel();
								emitter.complete();
							}

							if (contador == 5) {
								time.cancel();
								emitter.error(new InterruptedException("Error, se ha detenido el Flux en 5!!!"));
							}
						}
					}, 1000, 1000);
				})
//				.doOnComplete(() -> log.info("Hemos terminado!!!"))
//				Tambien podemos usar el doOnComplete en el subscribe ya que este tiene el 3 parametros:
//				subscribe((data) -> succces, (error) -> {}, () -> complete), el tercer callback se ejecuta siempre que termine
				.subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()), () -> log.info("Hemos terminado!!!"));
	}

	public void ejemploIntervaloInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(intervalo -> {
					if (intervalo >= 5) {
						return Flux.error(new InterruptedException("Solo hasta 5!!"));
					}
					return Flux.just(intervalo);
				})
				.map(intervalo -> "Hola " + intervalo)
				.retry(2)
				.subscribe(log::info, error -> log.error(error.getMessage()));

		latch.await();
	}


//	Nota: la idea de la programacion reactiva es no tener bloqueos los ejemplos con el blockLast es netamente practico.
	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(integer -> log.info(integer.toString()));

//		blockLast bloquea el hilo hasta que se muestre el ultimo elemento del flujo
		rango.blockLast();

//		Otra manera de hacerlo es pausando el hilo con .sleep
//		rango.subscribe();
//		Thread.sleep(130000);
	}

	public void ejemploInterval(){
		Flux<Integer> rango = Flux.range(1,12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(retraso, (ra, re) -> ra)
				.doOnNext(i -> log.info(i.toString()))
//				.subscribe();
				.blockLast();
	}

	public void ejemploUsuarioComentariosZipWithRangos() {
		Flux<Integer> rango = Flux.range(0,4);

		Flux.just(1,2,3,5)
				.map(number -> number * 2)
				.zipWith(rango, (numeroFlujo1, numeroFlujo2) -> String.format("primer flux: %d, segundo flux: %d", numeroFlujo1, numeroFlujo2))
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
