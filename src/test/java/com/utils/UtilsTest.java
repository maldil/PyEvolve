package com.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void getSortedList1() {
        List<Utils.Interval> duration = new ArrayList<>();
        duration.add(new Utils.Interval(2,5));
        duration.add(new Utils.Interval(3,5));
        duration.add(new Utils.Interval(6,8));
        duration.add(new Utils.Interval(10,12));
        duration.add(new Utils.Interval(20,32));
        duration.add(new Utils.Interval(21,31));
        Assertions.assertEquals(Utils.getSortedList(duration).toString(),"[[2,5], [6,8], [10,12], [20,32]]");
    }

    @Test
    void getSortedList2() {
        List<Utils.Interval> duration = new ArrayList<>();
        duration.add(new Utils.Interval(2,5));
        duration.add(new Utils.Interval(6,15));
        duration.add(new Utils.Interval(6,8));
        duration.add(new Utils.Interval(10,12));
        duration.add(new Utils.Interval(20,32));
        duration.add(new Utils.Interval(21,31));
        Assertions.assertEquals(Utils.getSortedList(duration).toString(),"[[2,5], [6,15], [20,32]]");
    }

    @Test
    void getSortedList3() {
        List<Utils.Interval> duration = new ArrayList<>();
        duration.add(new Utils.Interval(2,5));
        duration.add(new Utils.Interval(5,15));
        duration.add(new Utils.Interval(6,8));
        duration.add(new Utils.Interval(10,12));
        duration.add(new Utils.Interval(20,32));
        duration.add(new Utils.Interval(21,31));
        Assertions.assertEquals(Utils.getSortedList(duration).toString(),"[[2,15], [20,32]]");
    }
}