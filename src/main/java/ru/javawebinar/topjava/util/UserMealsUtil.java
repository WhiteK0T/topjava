package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class UserMealsUtil {
    public static void main(String[] args) throws InterruptedException {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        LocalTime startTime = LocalTime.of(7, 0);
        LocalTime endTime = LocalTime.of(12, 0);

        System.out.println(filteredByCycles(meals, startTime, endTime, 2000));
        System.out.println(filteredByStreams(meals, startTime, endTime, 2000));
        System.out.println(filteredByRecursive(meals, startTime, endTime, 2000));
        System.out.println(filteredByExecutor(meals, startTime, endTime, 2000));
        System.out.println(filteredByConsumer(meals, startTime, endTime, 2000));
        System.out.println(filteredByCollector(meals, startTime, endTime, 2000));
        System.out.println(filteredByPredicate(meals, startTime, endTime, 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final Map<LocalDate, Integer> mapDailyCalorie = new HashMap<>();
        meals.forEach(meal -> mapDailyCalorie.merge(meal.getDate(), meal.getCalories(), Integer::sum));

        final List<UserMealWithExcess> list = new ArrayList<>();
        meals.forEach(meal -> {
            if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                list.add(createMealWithExcess(meal, mapDailyCalorie.get(meal.getDate()) > caloriesPerDay));
            }
        });
        return list;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final Map<LocalDate, Integer> mapDailyCalorie = meals.stream()
                .collect(Collectors.toMap(UserMeal::getDate, UserMeal::getCalories, Integer::sum, HashMap::new));
        return meals.stream()
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime))
                .map(meal -> createMealWithExcess(meal, mapDailyCalorie.get(meal.getDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }

    public static List<UserMealWithExcess> filteredByConsumer(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final Map<LocalDate, Integer> mapDailyCalorie = new HashMap<>();
        final List<UserMealWithExcess> list = new ArrayList<>();
        Consumer<Void> consumer = unused -> {};

        for (UserMeal meal : meals) {
            mapDailyCalorie.merge(meal.getDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                consumer = consumer.andThen(unused -> list.add(createMealWithExcess(meal, mapDailyCalorie.get(meal.getDate()) > caloriesPerDay)));
            }
        }
        consumer.accept(null);
        return list;
    }

    public static List<UserMealWithExcess> filteredByPredicate(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final Map<LocalDate, Integer> mapDailyCalorie = new HashMap<>();
        final List<UserMealWithExcess> list = new ArrayList<>();
        Predicate<Boolean> predicate = unused -> true;

        for (UserMeal meal : meals) {
            mapDailyCalorie.merge(meal.getDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                predicate = predicate.and(unused -> list.add(createMealWithExcess(meal, mapDailyCalorie.get(meal.getDate()) > caloriesPerDay)));
            }
        }
        predicate.test(true);
        return list;
    }

    public static List<UserMealWithExcess> filteredByRecursive(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final List<UserMealWithExcess> list = new ArrayList<>();
        filterWithRecursion(meals, 0, startTime, endTime, caloriesPerDay, new HashMap<>(), list);
        return list;
    }

    private static void filterWithRecursion(List<UserMeal> meals, int count, LocalTime startTime, LocalTime endTime, int caloriesPerDay,
                                            Map<LocalDate, Integer> dailyCaloriesMap, List<UserMealWithExcess> result) {
        if (count >= meals.size()) return;
        UserMeal meal = meals.get(count++);
        dailyCaloriesMap.merge(meal.getDate(), meal.getCalories(), Integer::sum);
        filterWithRecursion(meals, count, startTime, endTime, caloriesPerDay, dailyCaloriesMap, result);
        if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
            result.add(createMealWithExcess(meal, dailyCaloriesMap.get(meal.getDate()) > caloriesPerDay));
        }
    }

    public static List<UserMealWithExcess> filteredByExecutor(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) throws InterruptedException {
        final Map<LocalDate, Integer> caloriesSumByDate = new HashMap<>();
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final List<UserMealWithExcess> mealsTo = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch latchCycles = new CountDownLatch(meals.size());

        for (UserMeal meal : meals) {
            caloriesSumByDate.merge(meal.getDate(), meal.getCalories(), Integer::sum);
            if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                executorService.submit(() -> {
                    latchCycles.await();
                    mealsTo.add(createMealWithExcess(meal, caloriesSumByDate.get(meal.getDate()) > caloriesPerDay));
                    return null;
                });
            }
            latchCycles.countDown();
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        return mealsTo;
    }

    private static List<UserMealWithExcess> filteredByCollector(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final class Aggregate {
            private final List<UserMeal> dailyMeals = new ArrayList<>();
            private int dailySumOfCalories;

            private void accumulate(UserMeal meal) {
                dailySumOfCalories += meal.getCalories();
                if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                    dailyMeals.add(meal);
                }
            }

            private Aggregate combine(Aggregate that) {
                this.dailySumOfCalories += that.dailySumOfCalories;
                this.dailyMeals.addAll(that.dailyMeals);
                return this;
            }

            private Stream<UserMealWithExcess> finisher() {
                final boolean excess = dailySumOfCalories > caloriesPerDay;
                return dailyMeals.stream().map(meal -> createMealWithExcess(meal, excess));
            }
        }

        Collection<Stream<UserMealWithExcess>> values = meals.stream()
                .collect(Collectors.groupingBy(UserMeal::getDate,
                        Collector.of(Aggregate::new, Aggregate::accumulate, Aggregate::combine, Aggregate::finisher))
                ).values();

        return values.stream().flatMap(Function.identity()).collect(toList());
    }

    private static UserMealWithExcess createMealWithExcess(UserMeal meal, boolean excess) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }
}