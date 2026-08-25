package com.example.bookwithticket.domain.performance.api;

public class KopisPerformanceResponse {

    public static class Item {
        private String mt20id;
        private String prfnm;
        private String prfpdfrom;
        private String prfpdto;
        private String fcltynm;
        private String poster;
        private String genrenm;
        private String prfstate;
        private String dtguidance;
        private Integer seatscale;
        private String sty;
        private String prfcast;
        private String pcseguidance;
        private String prfruntime;
        private String prfage;

        public Item() {}

        public Item(String mt20id, String prfnm, String fcltynm, String poster, String genrenm, Integer seatscale) {
            this(mt20id, prfnm, fcltynm, poster, genrenm, seatscale, null, null);
        }

        public Item(String mt20id, String prfnm, String fcltynm, String poster, String genrenm, Integer seatscale, String prfpdfrom, String dtguidance) {
            this(mt20id, prfnm, fcltynm, poster, genrenm, seatscale, prfpdfrom, dtguidance, null, null, null, null, null);
        }

        public Item(String mt20id, String prfnm, String fcltynm, String poster, String genrenm, Integer seatscale,
                    String prfpdfrom, String dtguidance, String sty, String prfcast, String pcseguidance, String prfruntime, String prfage) {
            this.mt20id = mt20id;
            this.prfnm = prfnm;
            this.fcltynm = fcltynm;
            this.poster = poster;
            this.genrenm = genrenm;
            this.seatscale = seatscale;
            this.prfpdfrom = prfpdfrom;
            this.dtguidance = dtguidance;
            this.sty = sty;
            this.prfcast = prfcast;
            this.pcseguidance = pcseguidance;
            this.prfruntime = prfruntime;
            this.prfage = prfage;
        }

        public String getMt20id() { return mt20id; }
        public void setMt20id(String mt20id) { this.mt20id = mt20id; }

        public String getPrfnm() { return prfnm; }
        public void setPrfnm(String prfnm) { this.prfnm = prfnm; }

        public String getPrfpdfrom() { return prfpdfrom; }
        public void setPrfpdfrom(String prfpdfrom) { this.prfpdfrom = prfpdfrom; }

        public String getPrfpdto() { return prfpdto; }
        public void setPrfpdto(String prfpdto) { this.prfpdto = prfpdto; }

        public String getFcltynm() { return fcltynm; }
        public void setFcltynm(String fcltynm) { this.fcltynm = fcltynm; }

        public String getPoster() { return poster; }
        public void setPoster(String poster) { this.poster = poster; }

        public String getGenrenm() { return genrenm; }
        public void setGenrenm(String genrenm) { this.genrenm = genrenm; }

        public String getPrfstate() { return prfstate; }
        public void setPrfstate(String prfstate) { this.prfstate = prfstate; }

        public String getDtguidance() { return dtguidance; }
        public void setDtguidance(String dtguidance) { this.dtguidance = dtguidance; }

        public Integer getSeatscale() { return seatscale; }
        public void setSeatscale(Integer seatscale) { this.seatscale = seatscale; }

        public String getSty() { return sty; }
        public void setSty(String sty) { this.sty = sty; }

        public String getPrfcast() { return prfcast; }
        public void setPrfcast(String prfcast) { this.prfcast = prfcast; }

        public String getPcseguidance() { return pcseguidance; }
        public void setPcseguidance(String pcseguidance) { this.pcseguidance = pcseguidance; }

        public String getPrfruntime() { return prfruntime; }
        public void setPrfruntime(String prfruntime) { this.prfruntime = prfruntime; }

        public String getPrfage() { return prfage; }
        public void setPrfage(String prfage) { this.prfage = prfage; }
    }
}
