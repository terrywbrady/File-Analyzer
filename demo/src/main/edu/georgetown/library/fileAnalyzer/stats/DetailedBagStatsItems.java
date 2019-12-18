package edu.georgetown.library.fileAnalyzer.stats;

import edu.georgetown.library.fileAnalyzer.util.APTrustHelper.STAT;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

public enum DetailedBagStatsItems implements StatsItemEnum {
    Key(StatsItem.makeStringStatsItem("Bag Path", 200)),
    Stat(StatsItem.makeEnumStatsItem(STAT.class, "Bag Status")),
    Count(StatsItem.makeIntStatsItem("Item Count")),
    BagSourceOrg(StatsItem.makeStringStatsItem("Source Org",150)),
    BagSenderDesc(StatsItem.makeStringStatsItem("Sender Desc",150)),
    BagSenderId(StatsItem.makeStringStatsItem("Sender Id",150)),
    BagCount(StatsItem.makeStringStatsItem("Bag Count",150)),
    BagTotal(StatsItem.makeStringStatsItem("Bag Total",150)),
    Message(StatsItem.makeStringStatsItem("Message",400)),
    ;
    StatsItem si;
    DetailedBagStatsItems(StatsItem si) {this.si=si;}
    public StatsItem si() {return si;}
}