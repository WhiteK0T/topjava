package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.repository.MealRepositoryInMemory;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class MealServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(MealServlet.class);
    private final MealRepository mealRepository;

    public MealServlet() {
        this.mealRepository = new MealRepositoryInMemory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        switch (action != null ? action : "") {
            case "delete":
                int id = Integer.parseInt(req.getParameter("id"));
                log.info("Delete ID = {}", id);
                mealRepository.delete(id);
                resp.sendRedirect("meals");
                break;
            case "create":
            case "update":
                Meal meal;
                if ("update".equals(action)) {
                    int idMeals = Integer.parseInt(req.getParameter("id"));
                    meal = mealRepository.get(idMeals);
                } else {
                    meal = new Meal(LocalDateTime.now(), "", 1);
                }
                req.setAttribute("meal", meal);
                req.getRequestDispatcher("/mealAdd.jsp").forward(req, resp);
                break;
            default:
                log.info("Get All Meals");
                List<MealTo> list = MealsUtil.getAll(mealRepository.getAll(), MealsUtil.DEFAULT_CALORIES_PER_DAY);
                req.setAttribute("meals", list);
                req.getRequestDispatcher("/meals.jsp").forward(req, resp);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        int id = req.getParameter("id") == null ? -1 : Integer.parseInt(req.getParameter("id"));

        Meal meal = new Meal(id, LocalDateTime.parse(req.getParameter("dateTime")),
                req.getParameter("description"), Integer.parseInt(req.getParameter("calories")));
        log.info(id == -1 ? "Create {}" : "Update {}", meal);
        mealRepository.add(meal);
        resp.sendRedirect("meals");
    }
}