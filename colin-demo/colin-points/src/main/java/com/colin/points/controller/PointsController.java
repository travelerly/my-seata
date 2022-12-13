package com.colin.points.controller;

import com.colin.points.service.PointsService;
import com.colin.points.service.TCCPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private TCCPointsService tccPointsService;

    /**
     * (AT 模式)增加积分
     * @param username
     * @param points
     */
    @GetMapping("/increase")
    public void increase(String username, Integer points) {
        pointsService.increase(username, points);
    }

    /**
     * (TCC 模式)增加积分
     * @param username
     * @param points
     */
    @GetMapping("/increaseTCC")
    public void increaseTCC(String username, Integer points) {
        tccPointsService.increase(username, points);
    }
}
