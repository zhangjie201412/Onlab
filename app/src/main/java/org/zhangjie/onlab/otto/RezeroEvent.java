package org.zhangjie.onlab.otto;

/**
 * Created by H151136 on 6/7/2016.
 */
public class RezeroEvent {
    public float start;
    public float end;
    public float speed;
    public float interval;

    public RezeroEvent(float start, float end, float speed, float interval) {
        this.start = start;
        this.end = end;
        this.speed = speed;
        this.interval = interval;
    }
}
