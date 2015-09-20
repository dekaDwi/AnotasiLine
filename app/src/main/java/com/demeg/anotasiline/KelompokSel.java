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
public class KelompokSel {

    Sel tail, head;
    String name;
    int count;

    public KelompokSel(String name) {
        tail = null;
        head = null;
        this.name = name;
        count = 0;
    }

    boolean isEmpty() {
        return head == null;
    }

    void addLast(Sel s) {
        count++;
        s.addName(name + "/" +count, "tanggal");
        if (isEmpty()) {
            head = s;
            tail = s;
        } else {
            tail.next = s;
            tail = s;
        }
    }

    void print() {
        Sel s = head;
        if (s.isEmpty()) {
            System.out.println("Kosong");
        } else {
            s.print();
            while (s.next != null) {
                s = s.next;
                s.print();
            }
        }
    }

    public String isi() {
        String isi = name;
        Sel s = head;
        if (isEmpty()) {
            isi = isi + "\nBelum ada sel yang tersimpan";
        } else {
            isi = isi + "\n" + s;
            while (s.next != null) {
                s = s.next;
                isi = isi + "\n" + s;
            }
        }
        return isi;
    }
}
