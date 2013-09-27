/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal;

import com.anosym.vjax.annotations.EnumMarkup;
import com.anosym.vjax.annotations.Marshallable;
import com.anosym.vjax.annotations.Position;
import java.util.HashMap;

/**
 *
 * @author Administrator
 */
@EnumMarkup("tof-field")
public enum VRecordResponseData {

    BANK_1_DEALING_CODE(508, "Bank 1 Dealing code"),
    BANK_1_NAME(509, "Bank 1 Name"),
    BANK_2_NAME(513, "Bank 2 Name"),
    BASE_CURRENCY(544, "Base Currency"),
    BASE_CURRENCY_2(581, "Base Currency 2"),
    BASE_CURRENCY_3(582, "Base Currency 3"),
    BROKER_DEALING_CODE(510, "Broker Dealing Code"),
    BROKER_NAME(511, "Broker Name"),
    CALCULATED_VOLUME_PERIOD_1_CURRENCY_2(545, "Calculated Volume period 1 Currency 2"),
    CLACULATED_VOLUME_PERIOD_2_CURRENCY_2(546, "Calculated Volume period 2 Currency 2"),
    COMMENT_TEXT(553, "Comment text"),
    CONFIRMED_BY(507, "Confirmed by"),
    CONFIRMED_BY_NAME(550, "Confirmed-by Name"),
    CONVERSATION_TEXT(548, "Conversation Text"),
    CURRENCY_1(517, "Currency 1"),
    CURRENCY_2(518, "Currency 2"),
    DATE_CONFIRMED(505, "Date Confirmed"),
    DATE_OF_DEAL(502, "Date of Deal"),
    DAYS_ELAPSED_DURING_DEAL(571, "Days Elapsed During Deal"),
    DEALER_ID(504, "Dealer ID"),
    DEALER_NAME(549, "Dealer name"),
    DEALING_SERVER_VERSION_NUMBER(558, "Dealing Server Version Number"),
    DEAL_TYPE(514, "Deal Type"),
    DEAL_VOLUME_CURRENCY_1(519, "Deal Volume Currency 1"),
    DEPOSIT_RATE(520, "Deposit Rate"),
    EXCHANGE_RATE_PERIOD_1(522, "Exchange Rate Period 1"),
    EXCHANGE_RATE_PERIOD_2(523, "Exchange Rate Period 2"),
    FRA_FIXING_DATE(554, "FRA Fixing Date"),
    FRA_MATURITY_DATE(556, "FRA Maturity Date"),
    FRA_SETTLEMENT_DATE(555, "FRA Settlement Date"),
    IMM_INDICATOR(557, "IMM Indicator"),
    INTEREST_MESSAGE(574, "Interest Message"),
    LATEST_DEAL_DATE(537, "Latest Deal Date"),
    LATEST_DEAL_IDENTIFIER(536, "Latest Deal Identifier"),
    LATEST_DEAL_TIME(538, "Latest Deal Time"),
    LOCAL_TCID(551, "Local TCID"),
    METHOD_OF_DEAL(540, "Method of Deal"),
    OLDEST_DEAL_DATE(534, "Oldest Deal Date"),
    OLDEST_DEAL_IDENTIFIER(533, "Oldest Deal Identifier"),
    OLDEST_DEAL_TIME(535, "Oldest Deal Time"),
    ORIGINAL_ID_FOR_CONTRA(567, "ID of Original if this is a Contra"),
    ORIGINAL_ID_FOR_NEXT(568, "ID of previous if this is 'next'"),
    OUTRIGHT_POINTS_PREMIUM_RATE(559, "Outright Points Premium Rate"),
    PAYMENT_INSTRUCTION_PERIOD_1_CURRENCY_1(529, "Payment instruction period 1 currency 1"),
    PAYMENT_INSTRUCTION_PERIOD_1_CURRENCY_2(530, "Payment instruction period 1 currency 2"),
    PAYMENT_INSTRUCTION_PERIOD_2_CURRENCY_1(531, "Payment instruction period 2 currency 1"),
    PAYMENT_INSTRUCTION_PERIOD_2_CURRENCY_2(532, "Payment instruction period 2 currency 2"),
    PERIOD_1(515, "Period 1"),
    PERIOD_2(516, "Period 2"),
    PRICE_CONVENTION(573, "Price Convention"),
    PURE_DEAL_TYPE(569, "Pure-Deal type"),
    RATE_BASE_CURRENCY_2_VERSUS_USD(583, "Rate Base Currency 2 versus USD"),
    RATE_BASE_CURRENCY_3_VERSUS_USD(584, "Rate Base Currency 3 versus USD"),
    RATE_BASE_CURRENCY_AGAINST_USD(543, "Rate Base Currency against USD"),
    RATE_CURRENCY_1_AGAINST_USD(541, "Rate Currency 1 against USD"),
    RATE_CURRENCY_2_AGAINST_USD(542, "Rate Currency 2 against USD"),
    RATE_DIRECTION(524, "Rate Direction"),
    REVIEW_REFERENCE_NUMBER(552, "Review Reference Number"),
    SECONDARY_SOURCE_REFERENCE(539, "Secondary source reference"),
    SOURCE_OF_DATA(500, "Source of Data"),
    SOURCE_REFERENCE(501, "Source Reference"),
    SPOT_BASIS_RATE(560, "Spot Basis Rate"),
    SPOT_MATCHING_CREDIT_REDUCTION(579, "Spot Matching Credit Reduction"),
    SPOT_MATCHING_CREDIT_REMAINING(580, "Spot Matching Credit Remaining"),
    SWAP_RATE(521, "Swap Rate"),
    SWIFT_BIC_CURRENCY_1_PERIOD_1(575, "SWIFT-BIC Currency 1 Period 1"),
    SWIFT_BIC_CURRENCY_1_PERIOD_2(577, "SWIFT-BIC Currency 1 Period 2"),
    SWIFT_BIC_CURRENCY_2_PERIOD_1(576, "SWIFT-BIC Currency 2 Period 1"),
    SWIFT_BIC_CURRENCY_2_PERIOD_2(578, "SWIFT-BIC Currency 2 Period 2"),
    TIME_CONFIRMED(506, "Time confirmed"),
    TIME_OF_DEAL(503, "Time of Deal"),
    USER_DEFINED_DATA_2(564, "User-defined Data 2"),
    USER_DEFINED_DATA_3(566, "User-defined Data 3"),
    USER_DEFINED_TITLE_1(561, "User-defined Title 1"),
    USER_DEFINED_TITLE_2(563, "User-defined Title 2"),
    USER_DEFINED_TITLE_3(565, "User-defined Title 3"),
    USER_DEFINED__DATA_1(562, "User-defined Data 1"),
    VALUE_DATE_PERIOD_1_CURRENCY_1(525, "Value date period 1 currency 1"),
    VALUE_DATE_PERIOD_1_CURRENCY_2(526, "Value date period 1 currency 2"),
    VALUE_DATE_PERIOD_2_CURRENCY_1(527, "Value date period 2 currency 1"),
    VALUE_DATE_PERIOD_2_CURRENCY_2(528, "Value date period 2 currency 2"),
    VOLUME_OF_INTEREST(570, "Volume of Interest"),
    VOLUME_PERIOD_2_CURRENCY_1(547, "Volume period 2 Currency 1"),
    YEAR_LENGTH(572, "Year Length"),
    TICKET_ID(200, "Ticket Id (Deal Id) for the current deal"),
    USER_DEFINED_600(600, "user defined"),
    USER_DEFINED_601(601, "user defined"),
    USER_DEFINED_602(602, "user defined"),
    USER_DEFINED_603(603, "user defined"),
    USER_DEFINED_604(604, "user defined"),
    USER_DEFINED_605(605, "user defined"),
    USER_DEFINED_606(606, "user defined"),
    USER_DEFINED_607(607, "user defined"),
    USER_DEFINED_608(608, "user defined"),
    USER_DEFINED_609(609, "user defined"),
    USER_DEFINED_610(610, "user defined"),
    USER_DEFINED_611(611, "user defined"),
    USER_DEFINED_612(612, "user defined"),
    USER_DEFINED_613(613, "user defined"),
    USER_DEFINED_614(614, "user defined"),
    USER_DEFINED_615(615, "user defined"),
    USER_DEFINED_616(616, "user defined"),
    USER_DEFINED_617(617, "user defined"),
    USER_DEFINED_618(618, "user defined"),
    USER_DEFINED_619(619, "user defined"),
    USER_DEFINED_620(620, "user defined"),
    USER_DEFINED_621(621, "user defined"),
    USER_DEFINED_622(622, "user defined"),
    USER_DEFINED_623(623, "user defined"),
    USER_DEFINED_624(624, "user defined"),
    USER_DEFINED_625(625, "user defined"),
    USER_DEFINED_626(626, "user defined"),
    USER_DEFINED_627(627, "user defined"),
    USER_DEFINED_628(628, "user defined"),
    USER_DEFINED_629(629, "user defined"),
    USER_DEFINED_630(630, "user defined"),
    USER_DEFINED_631(631, "user defined"),
    USER_DEFINED_632(632, "user defined"),
    USER_DEFINED_633(633, "user defined"),
    USER_DEFINED_634(634, "user defined"),
    USER_DEFINED_635(635, "user defined"),
    USER_DEFINED_636(636, "user defined"),
    USER_DEFINED_637(637, "user defined");
    public static int firstUserDefined = 600; //its 600, this is for testing
    public static int lastUserDefined = 632;

    public static HashMap<Integer, String> getUserDefinedFields() {
        /*
         * if possible check in a config file for default values
         */
        HashMap<Integer, String> fields = new HashMap<Integer, String>();
        for (int i = VRecordResponseData.firstUserDefined; i < lastUserDefined; i++) {
            fields.put(i, " ");
        }
        return fields;
    }

    private VRecordResponseData(int dataIndex, String name) {
        this.dataName = name;
        this.dataIndex = dataIndex;
    }
    private String dataName;
    private int dataIndex;

    @Marshallable(write = false)
    @Position(index = 0)
    public int getDataIndex() {
        return dataIndex;
    }

    @Marshallable(write = false)
    @Position(index = 1)
    public String getDataName() {
        return dataName;
    }

    public static VRecordResponseData findInstance(int dataIndex) {
        for (VRecordResponseData vr : values()) {
            if (vr.dataIndex == dataIndex) {
                return vr;
            }
        }
        return null;
    }

    @Marshallable(marshal = false)
    public boolean isDateResponseData() {
        return (this.dataIndex == 502
                || this.dataIndex == 505
                || this.dataIndex == 525
                || this.dataIndex == 526
                || this.dataIndex == 527
                || this.dataIndex == 528
                || this.dataIndex == 534
                || this.dataIndex == 537
                || this.dataIndex == 554
                || this.dataIndex == 555
                || this.dataIndex == 556);
    }

    @Marshallable(marshal = false)
    public boolean isTimeResponseData() {
        return (this.dataIndex == 503
                || this.dataIndex == 506
                || this.dataIndex == 503
                || this.dataIndex == 535
                || this.dataIndex == 538);
    }

    public String getSimpleName() {
        return this.name().replaceAll("_", " ");
    }
}
