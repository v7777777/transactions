package Bank;

import java.util.Objects;

public class Account implements Comparable <Account>
{
    private volatile long money;
    private String accNumber;
    private volatile Boolean isBloked = false;

    public Boolean getBloked() {
        return isBloked;
    }

    public void setBloked(Boolean bloked) {
        isBloked = bloked;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public String getAccNumber() {
        return accNumber;
    }

    public void setAccNumber(String accNumber) {
        this.accNumber = accNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return money == account.money && Objects.equals(accNumber, account.accNumber);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accNumber, money);
    }

    @Override
    public int compareTo(Account o) {
        return this.getAccNumber().compareTo(o.getAccNumber());
    }


}
