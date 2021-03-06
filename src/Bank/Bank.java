package Bank;

import java.util.HashMap;
import java.util.Random;

public class Bank
{

    private HashMap<String, Account> accounts = new HashMap <>();

    private final Random random = new Random();

    public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
        throws InterruptedException
    {
        Thread.sleep(1000);
        return random.nextBoolean();
    }


    public void transfer(String fromAccountNum, String toAccountNum, long amount) throws InterruptedException {

        // сделать проверку чтобы отсечь непдходящие опепрации до постановки в очередь

        if (accounts.get(fromAccountNum).getBloked() || accounts.get(toAccountNum).getBloked())
        { System.out.println("операция невозможна: один из счетов ранее был заблокирован " + fromAccountNum + " "+ toAccountNum); return;}



        Account lowSyncAccount;
        Account topSyncAccount;

        if (accounts.get(fromAccountNum).compareTo(accounts.get(toAccountNum)) > 0)
        {lowSyncAccount =  accounts.get(toAccountNum);
         topSyncAccount = accounts.get(fromAccountNum);
        }
        else {lowSyncAccount =  accounts.get(fromAccountNum);
              topSyncAccount = accounts.get(toAccountNum);}


       // synchronized(accounts.get(fromAccountNum).compareTo(accounts.get(toAccountNum)) > 0 ? fromAccountNum : toAccountNum) { // сначала по большему
       // synchronized(accounts.get(fromAccountNum).compareTo(accounts.get(toAccountNum)) > 0 ? toAccountNum :  fromAccountNum) { // потом по меньшему


        synchronized(topSyncAccount) {
        synchronized(lowSyncAccount) { 

        if (getBalance(fromAccountNum) < amount) { System.out.println("операция невозможна: недостаточно средств "  + fromAccountNum + ", баланс "
         + accounts.get(fromAccountNum).getMoney() + " сумма перевода  " + amount); return; }

        // если поток ожидал в очереди в других потоках мгли случится изенения этих счетов

        if (accounts.get(toAccountNum).getBloked() || accounts.get(fromAccountNum).getBloked())
        {System.out.println("операция невозможна: один из счетов ранее был заблокирован**** " + fromAccountNum + " " + toAccountNum); return; }


        long balanceFromBeforeTransaction = getBalance(fromAccountNum);
        long balanceToBeforeTransaction = getBalance(toAccountNum);

        accounts.get(fromAccountNum).setMoney(balanceFromBeforeTransaction - amount);
        accounts.get(toAccountNum).setMoney(balanceToBeforeTransaction + amount);

        System.out.println("перевод выполнен со счета " + fromAccountNum + ", баланс " + accounts.get(fromAccountNum).getMoney()
                    + " на счет " + toAccountNum + ", баланс " + accounts.get(toAccountNum).getMoney() + " сумма " + amount);


        if (amount > 50000 ) { safetyCheckTransaction(fromAccountNum, toAccountNum, amount);}


    }}}

    private void safetyCheckTransaction (String fromAccountNum, String toAccountNum, long amount) throws InterruptedException {

        if (isFraud(fromAccountNum, toAccountNum, amount))
        {
            ban(fromAccountNum,toAccountNum);

        }

    }

    private void ban (String fromAccountNum, String toAccountNum) {

        accounts.get(fromAccountNum).setBloked(true);
        accounts.get(toAccountNum).setBloked(true);
    }

    private long getBalance(String accountNum)  {
        return accounts.get(accountNum).getMoney();
    }

    public HashMap<String, Account> getAccounts() { return accounts; }




}
