package BackEnd;

public class Book {
    int bookNumber;
    int copiesNumber; // عدد النسخ كافة
    int borrowedCopies; //عدد النسخ المستعارة
    int height;
    Book LChild;
    Book RChild;
    String author;


    public Book(int bookNumber, int copiesNumber, int height, String author) {
        this.bookNumber = bookNumber;
        this.copiesNumber = copiesNumber;
        this.height = height;
        this.author = author;
        this.borrowedCopies = 0;
    }

    public int getAvailableCopies() {
        return getCopiesNumber() - getBorrowedCopies();
    }

    // تقرير من اجل النسخ المتاحة من كل كتاب
    public String AvailableBookCopies() {
        return String.format("Book Number: %-8s | Author: %-10s | Total Copies: %-5d | Available Copies: %-5d",
                getBookNumber(), getAuthor(), getCopiesNumber(), getAvailableCopies());
    }

    public int getBorrowedCopies() {
        return borrowedCopies;
    }

    public void setBorrowedCopies(int borrowedCopies) {
        this.borrowedCopies += borrowedCopies;
    }

    public int getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(int bookNumber) {
        this.bookNumber = bookNumber;
    }

    public int getCopiesNumber() {
        return copiesNumber;
    }

    public void setCopiesNumber(int copiesNumber) {
        this.copiesNumber += copiesNumber;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Book getLChild() {
        return LChild;
    }

    public void setLChild(Book LChild) {
        this.LChild = LChild;
    }

    public Book getRChild() {
        return RChild;
    }

    public void setRChild(Book RChild) {
        this.RChild = RChild;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
