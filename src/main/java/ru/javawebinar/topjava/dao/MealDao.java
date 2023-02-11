package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;

import java.util.Collection;

public interface MealDao {
    Meal add(Meal meal);
    boolean delete(int id);
    Meal get(int id);
    Collection<Meal> getAll();
}