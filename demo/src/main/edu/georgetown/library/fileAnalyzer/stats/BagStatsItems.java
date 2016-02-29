package edu.georgetown.library.fileAnalyzer.stats;

import edu.georgetown.library.fileAnalyzer.util.APTrustHelper.STAT;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

public enum BagStatsItems implements StatsItemEnum {
    Key(StatsItem.makeStringStatsItem("Source", 300)),
    Bag(StatsItem.makeStringStatsItem("Bag", 300)),
    Stat(StatsItem.makeEnumStatsItem(STAT.class, "Bag Status")),
    Count(StatsItem.makeIntStatsItem("Item Count")),
    Message(StatsItem.makeStringStatsItem("Message", 400)),
    ;
    StatsItem si;
    BagStatsItems(StatsItem si) {this.si=si;}
    public StatsItem si() {return si;}
}
