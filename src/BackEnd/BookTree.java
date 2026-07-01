package BackEnd;

import java.util.ArrayList;

import static java.lang.Math.max;

public class BookTree {
    static private Book Root = null;
    static public int allBook = 0;
    static public ArrayList<Book> Books = new ArrayList<>();

    public static ArrayList<Book> getBooks() {
        return Books;
    }

    //  البحث عن كتاب في الشجرة وارجاعه
    public static Book search(int number) {
        return search(Root, number);
    }

    private static Book search(Book cur, int number) {
        if (cur == null) return null;

        if (cur.bookNumber == number) return cur;

        else if (cur.bookNumber < number)
            return search(cur.RChild, number);
        else
            return search(cur.LChild, number);
    }

    // يرجع ارتفاع الكتاب في الشجرة
    private static int getHeight(Book b) {
        if (b == null) return 0;
        return b.height;
    }

    private static int getBalance(Book b) {
        if (b == null)
            return 0;
        return getHeight(b.LChild) - getHeight(b.RChild);
    }

    // LL Rotate
    private static Book rightRotate(Book b) {
        Book b2 = b.LChild;
        Book cur = b2.RChild;

        b2.RChild = b;
        b.LChild = cur;

        b.height = max(getHeight(b.RChild), getHeight(b.LChild)) + 1;
        b2.height = max(getHeight(b2.RChild), getHeight(b2.LChild)) + 1;

        return b2;
    }

    // RR Rotate
    private static Book leftRotate(Book b) {
        Book b2 = b.RChild;
        Book cur = b2.LChild;

        b2.LChild = b;
        b.RChild = cur;

        b.height = max(getHeight(b.RChild), getHeight(b.LChild)) + 1;
        b2.height = max(getHeight(b2.RChild), getHeight(b2.LChild)) + 1;

        return b2;
    }

    //اضافة كتاب جديد وارجاع النتيجة ( موجود مسبقا \ تمت العملية بنجاح )
    public static String insert(int number, int copies, String auth) {
        if (number <= 0) return "The Book number must be positive .";
        if (copies <= 0) return "The copies must be positive .";
        if (auth.isEmpty()) return "Author field required .";
        System.out.println("here");

        if (search(number) != null)
            return "The book already exists .";
        allBook++;
        Root = insert(Root, number, copies, auth);
        return "Done .";
    }

    private static Book insert(Book cur, int number, int copies, String auth) {
        if (cur == null) {
            cur = new Book(number, copies, 1, auth);
            Books.add(cur);
            return cur;
        }

        if (number > cur.bookNumber)
            cur.RChild = insert(cur.RChild, number, copies, auth);
        else if (number < cur.bookNumber)
            cur.LChild = insert(cur.LChild, number, copies, auth);
        else
            return cur;

        cur.height = max(getHeight(cur.LChild), getHeight(cur.RChild)) + 1;

        int balance = getBalance(cur);

        // LL case
        if (balance > 1 && number < cur.LChild.bookNumber)
            return rightRotate(cur);
            // RR case
        else if (balance < -1 && number > cur.RChild.bookNumber)
            return leftRotate(cur);
            // LR
        else if (balance > 1 && number > cur.LChild.bookNumber) {
            cur.LChild = leftRotate(cur.LChild);
            return rightRotate(cur);
        }
        // RL
        else if (balance < -1 && number < cur.RChild.bookNumber) {
            cur.RChild = rightRotate(cur.RChild);
            return leftRotate(cur);
        }

        return cur;
    }

    // يرجع اقل قيمة لكتاب في الشجرة
    private static Book minValue(Book b) {
        Book cur = b;
        while (cur.LChild != null)
            cur = cur.LChild;
        return cur;
    }

    //اضافة نسخ جديدة وارجاع النتيجة ( غير موجود الكتاب \ تمت العملية بنجاح )
    public static String addCopies(int bookNumber, int copies) {
        if (bookNumber <= 0) return "The Book number must be positive .";
        if (copies <= 0) return "The copies must be positive .";

        Book b = search(bookNumber);
        if (b == null) {
            return "There is no book with this number .";
        }
        b.setCopiesNumber(+copies);
        return "Done .";
    }

    //حذف نسخ من الكتاب وارجاع النتيجة ( غير موجود الكتاب \ لايوجد نسخ متوفرة للحذف \ تمت العملية بنجاح )
    public static String deleteCopies(int bookNumber, int copies) {
        if (bookNumber <= 0) return "The Book number must be positive .";
        if (copies <= 0) return "The copies must be positive .";

        Book b = search(bookNumber);
        if (b == null) {
            return "There is no book with this number .";
        }
        if (b.getCopiesNumber() - b.getBorrowedCopies() < copies) {
            return "Not enough copies to delete .";
        } else if (b.getCopiesNumber() - b.getBorrowedCopies() == copies && b.getBorrowedCopies() == 0) {
            return delete(bookNumber);
        } else {
            b.setCopiesNumber(-copies);
            return "Done .";
        }
    }

    // حذف كتاب وارجاع النتيجة ( تمت العملية بنجاح \ الكتاب غير موجود \ لا يمكن الحذف لان هناك نسخ مستعارة )
    public static String delete(int number) {
        if (number <= 0) return "The Book number must be positive .";

        Book cur = search(number);
        if (cur == null) {
            return "The book doesn't exist .";
        }
        if (cur.borrowedCopies != 0) {
            return "The book cannot be deleted because there are borrowed copies.";
        }
        Books.removeIf(Book -> Book.bookNumber == number);
        Root = delete(Root, number);
        allBook--;
        return "Done .";
    }

    private static Book delete(Book cur, int number) {
        if (cur == null) return cur;

        if (cur.bookNumber < number)
            cur.RChild = delete(cur.RChild, number);
        else if (cur.bookNumber > number)
            cur.LChild = delete(cur.LChild, number);
        else {
            // case 1 0 child
            if (cur.LChild == null || cur.RChild == null) {
                Book temp;
                if (cur.RChild != null)
                    temp = cur.RChild;
                else
                    temp = cur.LChild;

                cur = temp;
            }
            // case 2 children
            else {
                Book temp = minValue(cur.RChild);

                cur.bookNumber = temp.bookNumber;
                cur.copiesNumber = temp.copiesNumber;
                cur.author = temp.author;
                cur.borrowedCopies = temp.borrowedCopies;

                cur.RChild = delete(cur.RChild, temp.bookNumber);
            }
        }
        if (cur == null)
            return cur;

        cur.height = max(getHeight(cur.RChild), getHeight(cur.LChild)) + 1;
        int bal = getBalance(cur);

        // LL case
        if (bal > 1 && getBalance(cur.LChild) >= 0)
            return rightRotate(cur);
        // RR case
        if (bal < -1 && getBalance(cur.RChild) <= 0)
            return leftRotate(cur);
        // LR case
        if (bal > 1 && getBalance(cur.LChild) < 0) {
            cur.LChild = leftRotate(cur.LChild);
            return rightRotate(cur);
        }
        // RL case
        if (bal < -1 && getBalance(cur.RChild) > 0) {
            cur.RChild = rightRotate(cur.RChild);
            return leftRotate(cur);
        }
        return cur;
    }

    // استعارة كتاب
    public static String BorrowBook(int number) {
        if (number <= 0) return "The Book number must be positive .";

        Book b = search(number);
        if (b == null) return "The book doesn't exist .";

        if (b.getCopiesNumber() - b.getBorrowedCopies() == 0)
            return "There is no copy to borrow .";
        else {
            b.setBorrowedCopies(+1);
            return "Done .";
        }
    }

    // ارجاع كتاب مستعار
    public static String returnBook(int number) {
        if (number <= 0) return "The Book number must be positive .";

        Book b = search(number);
        if (b == null) return "The book doesn't exist .";

        if (b.getBorrowedCopies() <= 0) {
            return "There are no borrowed copies to return .";
        }

        b.setBorrowedCopies(-1);
        return "Done .";
    }

}