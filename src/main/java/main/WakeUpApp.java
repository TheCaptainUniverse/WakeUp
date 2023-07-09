package main;

import com.formdev.flatlaf.FlatIntelliJLaf;
import dao.TaskDao;
import org.apache.ibatis.session.SqlSession;
import pojo.TaskEntity;
import utils.MybatisUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class WakeUpApp extends JFrame
{
    private JPanel panel;
    private JTextField taskField;
    private JList<String> todoList;
    private DefaultListModel<String> listModel;
    private JPopupMenu popupMenu;
    private JMenuItem stickItem;
    private JMenuItem deleteItem;
    private Font taskFont;

    private static final TaskDao taskDao = MybatisUtils.getSession().getMapper(TaskDao.class);
    private static final SqlSession sqlSession = MybatisUtils.getSession();

    private static final Color COLOR_UNDO = new Color(0xC9C9C9);
    private static final Color COLOR_DONE = new Color(0x5C5C5C);

    private static final Color COLOR_FONT = new Color(0x242424);
    private static final Color COLOR_BACKGROUND = new Color(0xffffff);
    private static final Icon SELECTED_ICON = UIManager.getIcon("FileView.computerIcon");
    private static final HashMap<Integer, TaskEntity> taskMap = new HashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");


    public WakeUpApp()
    {
        setAlwaysOnTop(true);
        setTitle("待办任务提醒");
        setSize(320, 400);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // 获取屏幕的宽度和高度
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
        int screenWidth = screenBounds.width;
        int screenHeight = screenBounds.height;

        // 获取任务栏的高度
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        int taskbarHeight = screenInsets.bottom;

        // 设置窗口位置为除去任务栏的右下角
        int windowWidth = getWidth();
        int windowHeight = getHeight();
        int windowX = screenWidth - windowWidth;
        int windowY = screenHeight - windowHeight - taskbarHeight;
        setLocation(windowX, windowY);


        taskFont = new Font("黑体", Font.PLAIN, 20);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        todoList = new JList<>(listModel);
        todoList.setFont(taskFont);
        todoList.setBackground(COLOR_BACKGROUND);
        JScrollPane scrollPane = new JScrollPane(todoList);
        panel.add(scrollPane, BorderLayout.CENTER);

        taskField = new JTextField();
        panel.add(taskField, BorderLayout.SOUTH);


        getContentPane().add(panel);

        stickItem = new JMenuItem("置顶");
        deleteItem = new JMenuItem("删除");


        todoList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    int index = todoList.locationToIndex(e.getPoint());
                    if (index != -1)
                    {
                        todoList.setSelectedIndex(index);

                        JPopupMenu menu = getPopupMenu();

                        menu.show(todoList, e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int index = todoList.locationToIndex(e.getPoint());
                    if (index != -1)
                    {
                        JList<String> list = (JList<String>) e.getSource();
                        String task = list.getModel().getElementAt(index);
                        Color backgroundColor = list.getCellRenderer().getListCellRendererComponent(list, task, index, false, false).getBackground();
                        Color newColor = (backgroundColor == COLOR_UNDO) ? COLOR_DONE : COLOR_UNDO;
                        list.setSelectionBackground(newColor);
                        list.repaint();

                        changeTaskStatus(index);
                        reloadData();

                        listModel.setElementAt(task, index);
                    }
                }
            }
        });


        taskField.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addTask();
            }
        });

        todoList.setCellRenderer(new TodoListRenderer());

        todoList.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                taskField.requestFocusInWindow();
            }
        });

        todoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int selectedIndex = todoList.getSelectedIndex();
                    if (selectedIndex != -1)
                    {
                        todoList.repaint();
                    }
                }
            }
        });

        loadData();
    }

    private static void changeTaskStatus(int index)
    {
        TaskEntity taskEntity = taskMap.get(index);
        int status = taskEntity.getStatus();
        boolean finish = status == 1;
        if (!finish)
        {
            taskEntity.setDoneTime(LocalDateTime.now().format(formatter));
        } else
        {
            taskEntity.setDoneTime("");
        }
        taskEntity.setStatus(finish ? 0 : 1);
        taskDao.update(taskEntity);
    }

    private void loadData()
    {
        List<TaskEntity> taskList = taskDao.findAll();
        for (TaskEntity taskEntity : taskList)
        {
            String taskWithTimeInList = taskEntity.getCreateTime() + "\n" + taskEntity.getData();
            taskMap.put(taskEntity.getSort(), taskEntity);
            listModel.addElement(taskWithTimeInList);
        }
    }

    private JPopupMenu getPopupMenu()
    {
        popupMenu = new JPopupMenu();
        popupMenu.add(stickItem);
        popupMenu.add(deleteItem);
        stickItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedIndex = todoList.getSelectedIndex();
                if (selectedIndex != -1)
                {
//                    String selectedTask = listModel.getElementAt(selectedIndex);
//                    listModel.remove(selectedIndex);
//                    listModel.add(0, selectedTask);

                    List<TaskEntity> taskList = taskDao.findAll();
                    for (TaskEntity task : taskList)
                    {
                        int sort = task.getSort();
                        if (sort >= 0 && sort < selectedIndex)
                        {
                            task.setSort(sort + 1);
                            taskDao.update(task);
                        }
                    }
                    // 更新数据库中的排序
                    TaskEntity taskEntity = taskMap.get(selectedIndex);
                    taskEntity.setSort(0);
                    taskDao.update(taskEntity);
                    reloadData();
                }
            }
        });
        deleteItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedIndex = todoList.getSelectedIndex();
                if (selectedIndex != -1)
                {
                    TaskEntity taskEntity = taskMap.get(selectedIndex);
                    deleteTask(taskEntity);
                }
            }
        });

        return popupMenu;
    }

    private void deleteTask(TaskEntity taskEntity)
    {
        taskDao.delete(taskEntity);
        int selectedSort = taskEntity.getSort();

        // 更新数据库中的排序
        List<TaskEntity> taskList = taskDao.findAll();
        for (TaskEntity task : taskList)
        {
            int sort = task.getSort();
            if (sort > selectedSort)
            {
                task.setSort(sort - 1);
                taskDao.update(task);
            }
        }
        // 更新内存中的排序
        reloadData();
    }

    private void reloadData()
    {
        taskMap.clear();
        listModel.clear();
        loadData();
    }

    private void addTask()
    {
        String task = taskField.getText();
        if (!task.isEmpty())
        {
            LocalDateTime currentTime = LocalDateTime.now();
            String formattedTime = formatter.format(currentTime);

            // save to database
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setCreateTime(formattedTime);
            taskEntity.setData(task);
            taskEntity.setSort(taskMap.size());
            taskEntity.setStatus(0);
            taskDao.save(taskEntity);

            reloadData();
//            String taskWithTime = formattedTime + "\n" + task;
//            taskMap.put(taskMap.size(), taskEntity);
//            listModel.addElement(taskWithTime);
            taskField.setText("");
        }
    }

    class TodoListRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            TaskEntity taskEntity = taskMap.get(index);
            int status = taskEntity.getStatus();
            boolean finish = status == 1;
            Color backgroundColor = finish ? COLOR_DONE : COLOR_UNDO;
            component.setBackground(backgroundColor);
            component.setForeground(COLOR_FONT);
            component.setText("<html><body style='width: 230px'>" + formatTask(value.toString()) + "</body></html>");
            if (isSelected)
            {
                component.setIcon(SELECTED_ICON);
            } else
            {
                component.setIcon(null);
            }
            return component;
        }

        private String formatTask(String task)
        {
            // 将任务项按照每行 20 个字符进行换行
            StringBuilder sb = new StringBuilder();
            // 使用 StringTokenizer 将字符串按照空格分割
            StringTokenizer st = new StringTokenizer(task, " ");
            int charCount = 0;
            // 每次取出一个单词，判断是否超过 20 个字符，如果超过则换行
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                sb.append(token).append(" ");
                charCount += token.length() + 1;
                if (charCount >= 50)
                { // 设置每行最大字符数，根据需要进行调整
                    sb.append("<br>");
                    charCount = 0;
                }
            }
            return sb.toString();
        }
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException
    {
        System.setProperty("sun.java2d.noddraw", "true");
        FlatIntelliJLaf.install();
        UIManager.setLookAndFeel(new FlatIntelliJLaf());
        SwingUtilities.invokeLater(() ->
        {
            WakeUpApp app = new WakeUpApp();
            app.setVisible(true);
        });
    }
}




