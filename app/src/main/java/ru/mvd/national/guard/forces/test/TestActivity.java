package ru.mvd.national.guard.forces.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {

    private User user;

    private ArrayList<String> selectedItems;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private MenuItem next;

    private TextView fio;
    private TextView questionNumber;
    private TextView question;

    private int currentInx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarSetting(toolbar);

        try {
            user = new User(new DataSet(TestActivity.this), getIntent().getStringExtra("name"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        fio = (TextView)findViewById(R.id.fio);
        fio.setText(user.getUserName());
        question = (TextView)findViewById(R.id.question);
        questionNumber = (TextView)findViewById(R.id.questionNumber);

        selectedItems = new ArrayList<>();


        textFieldInitial();

        questionNumber.setText(getString(R.string.question_number_this) + (currentInx + 1));

    }

    private void toolbarSetting(Toolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test_activity, menu);
        next = menu.findItem(R.id.next);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.equals(next)) {
            byte selected = (byte)selectedItems.size();
            if (selected == 0 || selected > 1) {
                Toast.makeText(this, "Выберите 1 вариант ответа", Toast.LENGTH_SHORT).show();
            } else {
                if (selectedItems.get(0).equals(user.getTestQuestion(currentInx).getRightAns())) {
                    user.setRightAnswerCount(user.getRightAnswerCount() + 1);
                } else {
                    user.addIncorrectAnswerToList(currentInx, selectedItems.get(0));
                }
                selectedItems.clear();
                currentInx++;
                if (currentInx != user.getSize()) {
                    textFieldInitial();
                    questionNumber.setText(getString(R.string.question_number_this) + (currentInx + 1));
                } else {
                    if (user.getIncorrectAnswerSet().size() == 0) {
                        AlertDialog alertDialog = setBuilderSettingsSingle().create();
                        alertDialog.show();
                    } else {
                        AlertDialog alertDialog = setBuilderSettingsDouble().create();
                        alertDialog.show();
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog.Builder setBuilderSettingsDouble() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Тестирование завершено")
                .setMessage("Вы ответили правильно на " + user.getRightAnswerCount() + " из " + user.getSize() + " вопросов.")
                .setPositiveButton("Посмотреть ошибки", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                        ArrayList<DataFieldResult> list  = user.getIncorrectAnswerSet();
                        intent.putExtra("incorrect", list);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Завершить тест", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(TestActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setCancelable(false)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(TestActivity.this, "Выберите дальнейшее действие", Toast.LENGTH_LONG);
                    }
                });
        return builder;
    }

    private AlertDialog.Builder setBuilderSettingsSingle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Тестирование завершено")
                .setMessage("Вы ответили правильно на все вопросы!")
                .setPositiveButton("В главное меню", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(TestActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setCancelable(false)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(TestActivity.this, "Выберите дальнейшее действие", Toast.LENGTH_LONG).show();
                    }
                });
        return builder;
    }


    private void textFieldInitial() {
        question.setText(user.getTestQuestion(currentInx).getQuestion() + "\n" + user.getTestQuestion(currentInx).getRightAns());
        String[] items = {
                user.getTestQuestion(currentInx).getAns1(),
                user.getTestQuestion(currentInx).getAns2(),
                user.getTestQuestion(currentInx).getAns3(),
                user.getTestQuestion(currentInx).getAns4()
        };
        adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.questionItem, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = ((TextView)view).getText().toString();
                if (selectedItems.contains(selectedItem)) {
                    selectedItems.remove(selectedItem);
                } else {
                    selectedItems.add(selectedItem);
                }
            }
        });
    }

}
