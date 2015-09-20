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
public class Sel {

    public Elemen tail, head;
    public Sel next;
    public String name, time;
    public int count;

    public Sel() {
        tail = null;
        head = null;
        next = null;
        count = 0;
    }

    public void addName(String name, String time) {
        this.name = name;
        this.time = time;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void enQueue(int x, int y) {
        Elemen e = new Elemen(x, y);
        if (isEmpty()) {
            tail = e;
            head = e;
        } else {
            tail.next = e;
            tail = e;
        }
        count++;
    }

    public void deQueue() {
        if (isEmpty()) {
            System.out.println("Kosong");
        } else {
            System.out.println(head);
            head = head.next;
        }
    }

    public void print() {
        System.out.println(name);
        Elemen e = head;
        if (isEmpty()) {
            System.out.println("Kosong");
        } else {
            System.out.print(e);
            while (e.next != null) {
                e = e.next;
                System.out.print(" - " + e);
            }
            System.out.println("");
        }
    }

    public int[] getLast(){
        int[] a = {tail.x, tail.y};
        return a;
    }

    @Override
    public String toString() {
        String isi = name;
        Elemen e = head;
        if (isEmpty()) {
            isi = isi + " ==> Kosong";
        } else {
            isi = isi + " ==> " + e;
            while (e.next != null) {
                e = e.next;
                isi = isi + "; " + e;
            }
        }
        return isi;
    }
}
