package org.vaadin.marcus;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("")
public class TodoApp extends VerticalLayout {
  protected final TodoService service;
  protected final TextField task = new TextField("Task");
  protected final Binder<Todo> binder = new BeanValidationBinder<>(Todo.class);
  protected final UnorderedList taskList = new UnorderedList();

  TodoApp(TodoService service) {
    this.service = service;

    setWidth("90vmin");
    getStyle().set("margin", "auto");
    setDefaultHorizontalComponentAlignment(Alignment.CENTER);

    Button addButton = new Button("Add");
    HorizontalLayout form = new HorizontalLayout(task, addButton);
    form.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
    form.setFlexGrow(1, task);
    form.setWidth("100%");
    taskList.setWidth("100%");

    add(new H1("Todo"), form, taskList);

    binder.bindInstanceFields(this);
    addButton.addClickListener(this::addTask);
    task.addKeyPressListener(Key.ENTER, this::addTask);
    task.focus();
    updateTasks();
  }

  private void updateTasks() {
    taskList.removeAll();

    service.getTodos().forEach(todo -> {
      TextArea taskTextArea = createTaskTextArea(todo);
      Button deleteButton = new Button("Delete", e -> deleteTask(todo.getId()));
      deleteButton.setMinWidth("fit-content");
      HorizontalLayout taskLayout = new HorizontalLayout(taskTextArea, deleteButton);
      taskLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
      taskLayout.setFlexGrow(1, taskTextArea);
      taskList.add(new ListItem(taskLayout));
    });
  }

  private TextArea createTaskTextArea(Todo todo) {
    TextArea taskTextArea = new TextArea();
    taskTextArea.setValue(todo.getTask());
    taskTextArea.setReadOnly(true);
    taskTextArea.addFocusListener(e -> taskTextArea.setReadOnly(false));
    taskTextArea.addBlurListener(e -> taskTextArea.setReadOnly(true));
    taskTextArea.addKeyUpListener(Key.ENTER, e -> taskTextArea.blur());
    taskTextArea.addValueChangeListener(e -> {
      todo.setTask(e.getValue());
      service.saveTodo(todo);
    });

    return taskTextArea;
  }

  private <T extends ComponentEvent<?>> void addTask(T e) {
    Todo todo = new Todo();
    if (binder.writeBeanIfValid(todo)) {
      service.saveTodo(todo);
      binder.readBean(new Todo());
      updateTasks();
    }
  }

  private void deleteTask(Long id) {
    service.deleteTodo(id);
    updateTasks();
  }
}