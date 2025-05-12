package com.springboot.reactor.demo.models;

import java.util.List;

public class Comentarios {
    private List<String> comentarios;

    public Comentarios(List<String> comentarios) {
        this.comentarios = comentarios;
    }

    public void agregarComentario(String comentario){
        this.comentarios.add(comentario);
    }

    @Override
    public String toString() {
        return "comentarios: " + comentarios;
    }
}
