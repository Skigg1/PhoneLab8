package com.example.lab8;

public class EgeRequest {
    private int math;
    private int rus;
    private int inform;
    private int social;
    private int chemistry;
    private int physics;
    private int eng;
    private int geo;

    public EgeRequest(int math, int rus, int inform, int social,
                      int chemistry, int physics, int eng, int geo) {
        this.math = math;
        this.rus = rus;
        this.inform = inform;
        this.social = social;
        this.chemistry = chemistry;
        this.physics = physics;
        this.eng = eng;
        this.geo = geo;
    }

    public int getMath() { return math; }
    public int getRus() { return rus; }
    public int getInform() { return inform; }
    public int getSocial() { return social; }
    public int getChemistry() { return chemistry; }
    public int getPhysics() { return physics; }
    public int getEng() { return eng; }
    public int getGeo() { return geo; }
}