package com.example.asm_app_se06304.model;

public class Products {
    private int _id;
    private String _name;
    private String _image;
    private double _price;
    public Products(int id, String name, String image, double price) {
        this._id = id;
        this._name = name;
        this._image = image;
        this._price = price;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
    public void set_name(String _name) {
        this._name = _name;
    }
    public void set_image(String _image) {
        this._image = _image;
        }
    public void set_price(double _price) {
        this._price = _price;
    }
    public int get_id() {
        return _id;
    }
    public String get_name() {
        return _name;
        }
    public String get_image() {
        return _image;
    }
    public double get_price() {
        return _price;
    }

}
