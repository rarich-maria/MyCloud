package swing;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ModelListFile extends JTable {
    private Object[] columnsHeader = new String[] {"Имя файла", "Размер файла"};
    private DefaultTableModel tableModel;
    private JTable table;

    public ModelListFile (){
        table = new JTable();
        tableModel = new DefaultTableModel(null, columnsHeader) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(tableModel);
    }

    public void updateTable(String [][] data) {
        if (data == null) {
            System.out.println("null");
        } else {
            for (int i = 0; i < data.length; i++) {
                tableModel.addRow(data[i]);
                System.out.println(data[i][0] + "  " + data[i][1]);
            }
        }
    }

    public long getFileSize (int idx) {
        String[] subStr;
        String str =(String) table.getValueAt(idx, 1);
        String delimeter = " byte";
        subStr = str.split(delimeter);
        return Long.valueOf(subStr[0]);
    }

    public Integer searchEqualsFileName (String fileName) {
        Integer idx = null;
        for (int i =0; i<table.getRowCount(); i++) {
            String string = (String)table.getValueAt(i, 0);
            System.out.println(string+" " + string.length());
            if (fileName.equals(string)){
                idx = i;
                System.out.println("Совпадение номер строки " + i);
                break;
            }else {
                System.out.println("Совпадений нет");
            }
        }
        return idx;
    }

    public void removeFile (Integer idx) {
        if (idx!=null) {tableModel.removeRow(idx);}
    }

    public JTable getTable () {
        return this.table;
    }
}
