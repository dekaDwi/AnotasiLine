/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.demeg.anotasiline;

/**
 *
 * @author DeMeg
 */
public class Elemen {
    Elemen next;
    int x, y;
    
    Elemen(int x, int y){
        next = null;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
