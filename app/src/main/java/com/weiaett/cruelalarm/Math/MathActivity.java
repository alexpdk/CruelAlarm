package com.weiaett.cruelalarm.Math;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.weiaett.cruelalarm.R;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MathActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MathActivityAdapter mathActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math);


        TextView tvExpression = (TextView) findViewById(R.id.tvExpression);
        String[] expression = makeExpression();
        tvExpression.setText(expression[0] + " = ?");

        recyclerView = (RecyclerView) this.findViewById(R.id.grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        String[] answers = new String[10];
        for (int i = 0; i < 10; i++) {
            answers[i] = expression[i + 1];
        }
        mathActivityAdapter = new MathActivityAdapter(expression[0], answers, recyclerView);
        recyclerView.setAdapter(mathActivityAdapter);

        //  TODO
        //  Row alignment
        //  Closing activity upon solving
    }

    //  Возвращает выражение и ответ к нему
    private String[] makeExpression() {
        Random rand = new Random(System.currentTimeMillis());
        char operands[] = {'*', '/', '-', '+'};
        String[] output = new String[11];

        int firstNumber = rand.nextInt(500) + 300;
        int answer = firstNumber;
        output[0] = Integer.toString(firstNumber);

        char firstOperand = operands[rand.nextInt(2) + 2];
        output[0] += " " + Character.toString(firstOperand) + " ";

        char secondOperand = operands[rand.nextInt(2)];
        int secondNumber,
            thirdNumber = rand.nextInt(10) + 20;
        if (secondOperand == '/') {
            int tmp = rand.nextInt(5) + 5;
            if (firstOperand == '+')
                answer += tmp;
            else
                answer -= tmp;
            secondNumber = tmp * thirdNumber;
        } else {
            secondNumber = rand.nextInt(5) + 5;
            int mult = secondNumber * thirdNumber;
            if (firstOperand == '+')
                answer += mult;
            else
                answer -= mult;
        }

        output[0] += Integer.toString(secondNumber)
                  + ' ' + secondOperand + ' ' + thirdNumber;
        output[1] = Integer.toString(answer);

        HashSet<Integer> answers = new HashSet<>();
        answers.add(answer);
        while (answers.size() != 10)
            answers.add(rand.nextInt(900) + 100);
        answers.remove(answer);

        int i = 2;
        for (int ans : answers) {
            output[i++] = Integer.toString(ans);
        }
        return output;
    }
}
