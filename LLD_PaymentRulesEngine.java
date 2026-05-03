import java.time.LocalDateTime;
import java.util.*;

public class LLD_PaymentRulesEngine {
    public static void main(String[] args) {
        
    }
}

class PaymentsValidator {
    HashMap<String, User> userMap;
    RulesEngine rulesEngine;
    final double limit;

    public PaymentsValidator() {
        userMap = new HashMap<>();
        rulesEngine = new RulesEngine();
        limit = 1000.0;
    }

    public void initialiseUsers() {
        for(int i=0; i<10; ++i) {
            String userId = "User-"+i;
            userMap.put(userId, new User(userId));
        }
    }

    public void intialiseRules() {
        // initialiseCategoryRules
        // initialiseMerchantRules
        // initialiseCategoryWiseOverallTotalsRules


    }

    public boolean validatePayments(PaymentRequest paymentRequest) {
        String userId = paymentRequest.userId;
        if(!userMap.containsKey(userId)) {
            userMap.put(userId, new User(userId));
        }
        Spend spend = new Spend(paymentRequest.amt, paymentRequest.category, paymentRequest.merchant, paymentRequest.datetime);
        User user = userMap.get(userId);
        user.recordExpense(spend);

        // verify rules
        // the verification can also be done by placing these rules in a list

        // verify overall exp rules
        if(user.total > limit) {
            user.updateApprovalStatus(spend, false);
            return false;
        }

        // verify category wise overall exp rules
        boolean ret1 = rulesEngine.checkOverallCategoryWiseExp(user.categoryWiseTotals.get(paymentRequest.category), paymentRequest.category);
        if(!ret1) {
            user.updateApprovalStatus(spend, false);
            return false;
        }

        // verify category wise rules
        boolean ret2 = rulesEngine.checkCategoryRules(spend);
        if(!ret2) {
            user.updateApprovalStatus(spend, false);
            return false;
        }

        // verify merchant wise rules
        boolean ret3 = rulesEngine.checkMerchantWiseRules(spend);
        if(!ret3) {
            user.updateApprovalStatus(spend, false);
            return false;
        }

        // return true
        user.updateApprovalStatus(spend, true);
        return true;
    }
}

class PaymentRequest {
    String userId;
    String category;
    String merchant;
    double amt;
    LocalDateTime datetime;
}

class User {
    String userId;
    // category wise totals
    HashMap<String, Double> categoryWiseTotals;
    List<Expenditure> expenditures; 
    double total;

    User(String userId) {
        this.userId = userId;
        categoryWiseTotals = new HashMap<>();
        expenditures = new LinkedList<>();
        total = 0.0;
    }

    public void recordExpense(Spend spend) {
        categoryWiseTotals.put(spend.category, categoryWiseTotals.getOrDefault(spend.category, 0.0) + spend.amt);
        total += spend.amt;
    }

    public void updateApprovalStatus(Spend spend, boolean isApproved) {
        expenditures.add(new Expenditure(spend, isApproved));
    }
}

class Expenditure {
    Spend spend;
    boolean isApproved;

    public Expenditure(Spend spend, boolean isApproved) {
        this.spend = spend;
        this.isApproved = isApproved;
    }

    public Expenditure() {
    }
}

class RulesEngine {
    HashMap<String, Rule> categoryRulesMap;
    HashMap<String, Rule> merchantRulesMap;
    HashMap<String, Double> maxCategoryExpRulesMap; 

    public RulesEngine() {
        categoryRulesMap = new HashMap<>();
        merchantRulesMap = new HashMap<>();
        maxCategoryExpRulesMap = new HashMap<>();

    }

    // addCategoryRule()
    // addMerchantRule()
    // addCategoryWiseOverallExpRule()

    // deleteRules()

    // returns true if a spend is valid
    public boolean checkCategoryRules(Spend spend) {
        String category = spend.category;
        if(!categoryRulesMap.containsKey(category)) {
            return true;
        }
        Rule rule = categoryRulesMap.get(category);
        if(rule.ruleType == RuleType.BAN) {
            return false;
        }
        else if(rule.ruleType == RuleType.GREATER) {
            if(spend.amt > rule.amt) {
                return false;
            } 
        }
        return true;
    }

    // check overall exp per category 
    public boolean checkOverallCategoryWiseExp(double totalExpPerCat, String category) {
        if(maxCategoryExpRulesMap.containsKey(category)) {
            double permissibleAmt = maxCategoryExpRulesMap.get(category);
            if(totalExpPerCat > permissibleAmt) {
                return false;
            }
        }
        return true;
    }

    // check merchant wise rules
    public boolean checkMerchantWiseRules(Spend spend) {
        if(merchantRulesMap.containsKey(spend.merchant)) {
            Rule rule = merchantRulesMap.get(spend.merchant);
            if(rule.ruleType == RuleType.BAN) {
                return false;
            }
            if(rule.ruleType == RuleType.GREATER) {
                if(spend.amt > rule.amt) {
                    return false;
                }
            }
        }
        return true;
    }
}

class Spend {
    double amt;
    String category;
    String merchant;
    LocalDateTime dateTime;

    public Spend() {
    }

    public Spend(double amt, String category, String merchant, LocalDateTime dateTime) {
        this.amt = amt;
        this.category = category;
        this.merchant = merchant;
        this.dateTime = dateTime;
    }
}

class Rule {
    RuleType ruleType;
    double amt;

    Rule(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Rule(RuleType ruleType, double amt) {
        this.ruleType = ruleType;
        this.amt = amt;
    }
}

enum RuleType {
    BAN, GREATER, LESSER
}

/*

class PaymentsValidator -- contains object of RuleEngine and Map of Users and verifies every payment
class User -- will contain the spend details, overall expenditure and per category
class RuleEngine -- which will store the rules in maps
class Rule -- type of rule, decimal value

enum RuleType -- BAN, GREATER

utility classes -- Spend, Expenditure

*/