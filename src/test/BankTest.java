package test;

import Bank.Bank;
import Bank.Account;
import junit.framework.TestCase;
import java.util.ArrayList;


public class BankTest extends TestCase {

    final static long maxTransferAmount = 100000;

    final static long maxAccountMoneyAmount = 200000;

    final static int amountOfAccounts = 300;

    final static int amountOfTransactions = 1000;

    ArrayList<Thread> transactions;

    Bank multiThreadBank; // для проверки в многопоточном режиме без метода фрод

    Bank oneThreadBank; // для проверки в однопоточном режиме

    Bank allTransactionsBank; // для проверки в многопоточном режиме со всеми условиями

    ArrayList<Thread> transactionsAll;

    ArrayList<ArrayList<String>> transactionsLog; // все операции которые пройдут в многопоточном и однопоточном режиме

    Bank deadlockCase;  // для проверки взаимной блокировки

    Bank blockedAndNoBalanceCases; // для проверки условий недостаточно средств для перевода или блокирования небезоп опер

    @Override
    public void setUp() throws Exception {

        transactions = new ArrayList<>(); // суммы до 50т

        transactionsAll = new ArrayList<>(); // все суммы

        multiThreadBank = new Bank();

        oneThreadBank = new Bank();

        allTransactionsBank = new Bank();

        transactionsLog = new ArrayList <>();  // все транзакции: accFrom AccTo Amount strings

        createAccounts(amountOfAccounts, multiThreadBank, 1); // 1 = заполнить oneThreadBank

        setTransactions(amountOfTransactions, multiThreadBank, transactions, 1); // 1 = транзакции меньше 50т

        createAccounts(amountOfAccounts, allTransactionsBank, 0); // 0 = вкл транзакции больше 50т

        setTransactions(amountOfTransactions, allTransactionsBank,  transactionsAll, 0);  // 0 = не заполнять oneThreadBank

        deadlockCase = new Bank();

        deadlockCase.getAccounts().put("ac1", new Account());
        deadlockCase.getAccounts().put("ac2", new Account());
        deadlockCase.getAccounts().get("ac1").setMoney(2000000);
        deadlockCase.getAccounts().get("ac2").setMoney(2000000);
        deadlockCase.getAccounts().get("ac1").setAccNumber("ac1");
        deadlockCase.getAccounts().get("ac2").setAccNumber("ac2");


        blockedAndNoBalanceCases = new Bank();

        blockedAndNoBalanceCases.getAccounts().put("ac1", new Account());
        blockedAndNoBalanceCases.getAccounts().put("ac2", new Account());
        blockedAndNoBalanceCases.getAccounts().get("ac1").setMoney(100000);
        blockedAndNoBalanceCases.getAccounts().get("ac2").setMoney(1000000);
        blockedAndNoBalanceCases.getAccounts().get("ac1").setAccNumber("ac1");
        blockedAndNoBalanceCases.getAccounts().get("ac2").setAccNumber("ac2");


    }

    // не сработает условие недостаточно средств или счет залокирован
    // тк размер транзакций до 500 р и большие балансы счетов, не уйдут в минус

    public void test_multithread_transactions_less_than_50k_transfer () throws InterruptedException {

        calculateInOneThread();

        startTransactions(transactions);

        Thread.sleep(50000);

//        System.out.println("------------oneThreadBank---------------------------------");
//
//        oneThreadBank.getAccounts().values().forEach(a -> System.out.println(a.getAccNumber() + " " + a.getMoney()));
//
//        System.out.println("------------multiThreadBank---------------------------------");
//
//        multiThreadBank.getAccounts().values().forEach(a -> System.out.println(a.getAccNumber() + " " + a.getMoney()));

        assertEquals(multiThreadBank.getAccounts(), oneThreadBank.getAccounts());

    }

    // срабатывают все условия но тк в однопоточном режиме фрод метод может выдать иные результаты проверок
    // то конечные баланс в одно и много поточных режимах не совпадут
    // но если нет ошибок сумма всех балансов до и после должна остаться одинаковой

    public void test_multithread_transactions_all() throws InterruptedException {

      Long sumBefore =  allTransactionsBank.getAccounts().values().stream().map(v -> v.getMoney()).reduce((s1, s2) -> s1+s2 ).get();

      startTransactions(transactionsAll);

    Thread.sleep(30000);

    Long sumAfter =  allTransactionsBank.getAccounts().values().stream().map(v -> v.getMoney()).reduce((s1, s2) -> s1+s2 ).get();

        assertEquals(sumBefore, sumAfter);

    }

    // теcт взаимной блокировки

    public void test_deadlock() throws InterruptedException {

        Thread t1 = new Thread();
        Thread t2 = new Thread();

        for (int i = 0; i < 10; i ++) {

             t1 = new Thread(()-> {
                try {
                    deadlockCase.transfer("ac1", "ac2", 50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            t1.start();

             t2 = new Thread(()-> {
                try {
                    deadlockCase.transfer("ac2", "ac1", 100000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


           t2.start();

        }

        Thread.sleep(10000);

        assertEquals(t1.getState(), Thread.State.TERMINATED);
        assertEquals(t2.getState(), Thread.State.TERMINATED);



    }

  //   тест недостаточно средств
  //   тест ак заблокирован ранее

    public void test_baned_account_and_not_enough_money_cases() throws InterruptedException {

        for (int i = 0; i< 4; i++) {

            blockedAndNoBalanceCases.transfer("ac1", "ac2", 49000);

        }

        assertEquals(blockedAndNoBalanceCases.getAccounts().get("ac1").getMoney(), 2000);

        for (int i = 0; i< 15; i++) {

            blockedAndNoBalanceCases.transfer("ac2", "ac1", 60000);

        }

        assertTrue(blockedAndNoBalanceCases.getAccounts().get("ac2").getBloked());
    }

// -------------- методы для заполнения банков для тестов  -------------

    private void createAccounts(int amountOfAccounts, Bank bank, int param ) {

        for (int i = 0; i < amountOfAccounts; i++) {

            Account account = new Account();

            account.setAccNumber("Bank.Account " + ((Integer)(i+1)).toString());

            long money = 90000 + (long) (Math.random()*(maxAccountMoneyAmount+1));

            account.setMoney(money);

            bank.getAccounts().put(account.getAccNumber(), account);

          if (param == 1)  {Account accountOneThread = new Account();
            accountOneThread.setAccNumber(account.getAccNumber());
            accountOneThread.setMoney(account.getMoney());
            oneThreadBank.getAccounts().put(accountOneThread.getAccNumber(), accountOneThread);}



        }



    }

    private void setTransactions (int amountOfTransactions, Bank bank,  ArrayList<Thread> transactions,  int param) {

        String [] accounts  = bank.getAccounts().keySet().toArray(new String [bank.getAccounts().keySet().size()]);

        for (int i = 0; i < amountOfTransactions; i++)

        {
            int randomAcFrom;
            int randomAcTo;

            for (;;) {

                randomAcFrom = 0 + (int) (Math.random()*(bank.getAccounts().keySet().size())); // от 0 до размера сета со счетами
                randomAcTo = 0 + (int) (Math.random()*(bank.getAccounts().keySet().size()));

                if (randomAcFrom != randomAcTo) { break; } }


            String currentFrom = accounts[randomAcFrom];
            String currentTo = accounts[randomAcTo];

            long currentAmount;

           if (param == 1) {   currentAmount = getTransactionAmount(i, amountOfTransactions);}
           else {  currentAmount = getTransactionAmountAll (i, amountOfTransactions); }

            ArrayList <String> currentTransactionToLog = new ArrayList<>() {{

                    add(currentFrom);
                    add(currentTo);
                    add(((Long)currentAmount).toString());

            }};

            transactionsLog.add(currentTransactionToLog);

            transactions.add(new Thread(() -> {
                try {
                    bank.transfer(currentFrom, currentTo, currentAmount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));


        }
    }

    private void startTransactions(ArrayList<Thread> transactions) {

        transactions.forEach(t -> t.start());
    }

    private long getTransactionAmount(int i, int amountOfTransactions) {

        return (long) (1 + Math.random()*(500));

        }

    private long  getTransactionAmountAll(int i, int amountOfTransactions) {

         if (i > amountOfTransactions*0.05 ) { return (long) (1 + Math.random()*(50000));    }
         else { return (long) (50000 + Math.random()*(maxTransferAmount + 1)); }



    }

    private void calculateInOneThread() throws InterruptedException {

        for (int i = 0; i < amountOfTransactions; i++) {

            oneThreadBank.transfer(transactionsLog.get(i).get(0), transactionsLog.get(i).get(1), Long.parseLong(transactionsLog.get(i).get(2)));
        } }







}





