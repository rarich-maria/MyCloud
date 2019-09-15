package swing;

import client.auth.AuthException;

import javax.swing.*;

public class MessageDialogWindow {
    private MainWindow parent;

    public MessageDialogWindow (MainWindow parent) {
        this.parent = parent;
    }

    public void messageDialogRowNotSelected(String s) {
        JOptionPane.showMessageDialog(parent,
                  s, "Внимание!",
                  JOptionPane.PLAIN_MESSAGE);
    }

    public Integer showMessageFileExist(String fileName) {
        Integer idx = null;
        int result = JOptionPane.showConfirmDialog(
                  parent,
                  "Файл " + fileName + " уже существует на сервере, заменить?", "Внимание!",
                  JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            System.out.println("Выбрана замена файла rowcount " + parent.getTableFiles().getTable().getRowCount());
            return parent.searchEqualsFileName(fileName);
        } else if (result == JOptionPane.NO_OPTION) {
            System.out.println("Выбрана отмена загрузки файла");
        }
        return idx;
    }

    public boolean showMessageForLocalStorage(String fileName) {
        boolean res = false;
        int result = JOptionPane.showConfirmDialog(
                  parent,
                  "Файл " + fileName + " уже существует в локальном хранилище, заменить?", "Внимание!",
                  JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            System.out.println("Выбрана замена файла");
            res = true;
        } else if (result == JOptionPane.NO_OPTION) {
            System.out.println("Выбрана отмена загрузки файла");
        }
        return res;
    }

    public boolean showMessageForCloseWindow() throws InterruptedException {
        boolean res = false;
        int result = JOptionPane.showConfirmDialog(
                  parent,
                  "Загрузка фала (-ов) не завершена. При выходе из программы загрузка будет остановлена, а файлы удалены", "Внимание!",
                  JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            res = true;
        } else if (result == JOptionPane.NO_OPTION) {
            System.out.println("Отмена выхода, продолжение загрузки");
        }
        return res;
    }

    public void showMessageDownloadCompleted(String fileName) {
        JOptionPane.showMessageDialog(parent,
                  "Скачивание файла " + fileName + " завершено",
                  "Скачивание завершено",
                  JOptionPane.PLAIN_MESSAGE);
    }

    public boolean showMessageDeleteDownloadFile(String fileName) {
        int result = JOptionPane.showConfirmDialog(
                  parent,
                  "Вы действительно хотите остановить загрузку и удалить Файл " + fileName + "?", "Внимание!",
                  JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            return true;
        } else if (result == JOptionPane.NO_OPTION) {
            System.out.println("Выбрано продолжение загрузки файла");
        }
        return false;
    }

    public boolean showMessageUnloadedFile() {
        int result = JOptionPane.showConfirmDialog(
                  parent,
                  "На сервере имеются незагруженные файлы. Продолжить загрузку? При выборе отмены файлы будут удалены", "Внимание!",
                  JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            return true;
        } else if (result == JOptionPane.NO_OPTION) {
            System.out.println("Выбрано продолжение загрузки файла");
        }
        return false;
    }

    public void messageError (Throwable cause) {
         if (cause instanceof Exception){
            JOptionPane.showMessageDialog(parent,
                      "Ошибка сети",
                      "Авторизация",
                      JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
}
