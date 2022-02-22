public class Main01_PPS {
    public static void main(String[] args) {

        // import the configuration from the XML
        PPS pps1 = PPS.importFromXML("HvA2018_e10_p25.xml");
        pps1.printPlanningStatistics();

        // import the configuration from the XML
        PPS pps2 = PPS.importFromXML("HvA2019_e50_p100.xml");
        pps2.printPlanningStatistics();
    }
}
