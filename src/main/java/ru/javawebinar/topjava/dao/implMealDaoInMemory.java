package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class implMealDaoInMemory implements MealDao {
    private final Map<Integer, Meal> map = new ConcurrentHashMap<>();
    private final AtomicInteger idCount = new AtomicInteger(0);

    public implMealDaoInMemory() {
        MealsUtil.meals.forEach(this::add);
    }

    @Override
    public Meal add(Meal meal) {
        if (meal.getId() == -1) {
            meal.setId(idCount.incrementAndGet());
            map.put(meal.getId(), meal);
            return meal;
        }
        return map.computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
    }

    @Override
    public boolean delete(int id) {
        return map.remove(id) != null;
    }

    @Override
    public Meal get(int id) {
        return map.get(id);
    }

    @Override
    public Collection<Meal> getAll() {
        return map.values();
    }
}