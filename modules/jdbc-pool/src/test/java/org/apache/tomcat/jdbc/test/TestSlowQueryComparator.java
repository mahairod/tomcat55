package org.apache.tomcat.jdbc.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport.QueryStats;
import org.junit.Assert;
import org.junit.Test;

public class TestSlowQueryComparator {

    @Test
    public void testBug58489() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException {

        long[] testData = { 0, 0, 0, 1444225382010l, 0, 1444225382011l, 0,
                1444225382012l, 0, 1444225382056l, 0, 1444225382014l, 0,
                1444225382015l, 0, 1444225382016l, 0, 0, 1444225382017l, 0,
                1444225678350l, 0, 1444225680397l, 0, 1444225382018l,
                1444225382019l, 1444225382020l, 0, 1444225382021l, 0,
                1444225382022l, 1444225382023l

        };

        List<QueryStats> stats = new ArrayList<>();

        for (int i = 0; i < testData.length; i++) {
            QueryStats qs = new QueryStats(String.valueOf(i));
            qs.add(0, testData[i]);
            stats.add(qs);
        }

        try {
            Collections.sort(stats, createComparator());
        } catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testEqualQueryStatsWithNoLastInvocation()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Comparator<QueryStats> queryStatsComparator = createComparator();
        QueryStats q1 = new QueryStats("abc");
        Assert.assertEquals(0, queryStatsComparator.compare(q1, q1));
    }

    @Test
    public void testEqualQueryStatsWithLastInvocation()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Comparator<QueryStats> queryStatsComparator = createComparator();
        QueryStats q1 = new QueryStats("abc");
        q1.add(0, 100);
        Assert.assertEquals(0, queryStatsComparator.compare(q1, q1));
    }

    @Test
    public void testQueryStatsOneWithLastInvocation()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Comparator<QueryStats> queryStatsComparator = createComparator();
        QueryStats q1 = new QueryStats("abc");
        QueryStats q2 = new QueryStats("def");
        q2.add(0, 100);
        Assert.assertEquals(-1, queryStatsComparator.compare(q1, q2));
        Assert.assertEquals(1, queryStatsComparator.compare(q2, q1));
    }

    @Test
    public void testQueryStatsBothWithSameLastInvocation()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Comparator<QueryStats> queryStatsComparator = createComparator();
        QueryStats q1 = new QueryStats("abc");
        QueryStats q2 = new QueryStats("def");
        q1.add(0, 100);
        q2.add(0, 100);
        Assert.assertEquals(0, queryStatsComparator.compare(q1, q2));
        Assert.assertEquals(0, queryStatsComparator.compare(q2, q1));
    }

    @Test
    public void testQueryStatsBothWithSomeLastInvocation()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Comparator<QueryStats> queryStatsComparator = createComparator();
        QueryStats q1 = new QueryStats("abc");
        QueryStats q2 = new QueryStats("abc");
        q1.add(0, 100);
        q2.add(0, 150);
        Assert.assertEquals(-1, queryStatsComparator.compare(q1, q2));
        Assert.assertEquals(1, queryStatsComparator.compare(q2, q1));
    }

    private Comparator<QueryStats> createComparator()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        Class<?> comparatorClass = Class
                .forName("org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport$QueryStatsComparator");
        Constructor<?> comparatorConstructor = comparatorClass
                .getDeclaredConstructors()[1];
        comparatorConstructor.setAccessible(true);
        @SuppressWarnings("unchecked")
        Comparator<QueryStats> queryStatsComparator = (Comparator<QueryStats>) comparatorConstructor
                .newInstance();
        return queryStatsComparator;
    }

}
