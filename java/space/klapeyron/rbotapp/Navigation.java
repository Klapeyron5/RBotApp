package space.klapeyron.rbotapp;

public class Navigation
{
    Navigation() {}

    int[][] landscape = {{1,1,1,1,1},
                         {1,0,0,0,1},
                         {1,0,1,0,1},
                         {1,0,0,0,1},
                         {1,1,1,1,1}}; // Ландшафт карты. Нули обозначают разрешенные области, единицы - запрещенные.
    int m = landscape.length; //длина карты
    int n = landscape[0].length; // высота карты

    int[] start = {2,2}; // Начальная точка
    int[] finish = {4,4}; // Конечная точка
    int[] tick = start; // Текущая точка (изначально равна начальной)
    int[][] neighbour = new int[4][3]; // Матрица координат (первые две строки) и значений эвристической функции (третья строка) четырех точек-соседей (столбец соответсвует номеру)
    int F; // Эвристическая функция
    int f;
    int x,y;
    int i,j;

    /*ArrayList consideredPoints = new ArrayList();
    ArrayList newPoints = new ArrayList(); // Попытка создать динамический массив*/

    int[][] consideredPoints = new int[m*n][4]; // Массив рассмотренных точек (максимально их количество может быть равно m*n).
   // int[][] newPoints = new int[m*n][4]; // Первые две строчки - координаты рассматриваемой точки. Вторые две - Координаты "материнской" точки, на которую ссылается данная.

    private void getNeighbours() //Нахождение соседей текущей точки
    {
        neighbour[0][0] = tick[0]+1; neighbour[0][1] = tick[1];
        neighbour[1][0] = tick[0]; neighbour[1][1] = tick[1]-1;
        neighbour[2][0] = tick[0]-1; neighbour[2][1] = tick[1];
        neighbour[3][0] = tick[0]; neighbour[3][1] = tick[1]+1;
    }
    private int getFunction() //Нахождение эвристической функции оптимального продолжения маршрута
    {
        getNeighbours();
        F = Math.abs(neighbour[i][0]-finish[0]) + Math.abs(neighbour[i][1] - finish[1]) + (m+n+1)*landscape[tick[0]][tick[1]];
        return F;
    }
    private void getPoint() //Нахождение оптимальной точки продолжения пути из текущей
    {
        for (i=0;i<=3;i++)
        {
            neighbour[i][2] = getFunction(); // Для каждого "соседа" узнаем функцию F
        }

        f = neighbour[0][2]; //здесь и далее находим минимальное значение f функции F

        for(i=1;i<=3;i++)
        {
            if (neighbour[i][2]<neighbour[i-1][2])
            {
                f = neighbour[i][2]; // Номер соседа с наименьшим значением функции.
                j = i;
            }
        }
        for(i=3;i>j;i--)
        {
            if (neighbour[i][2]==f) // Если находим двух соседей с наименьшим значением функции, то выбираем исходя из того, у кого номер больше.
            {
                if(i>j)
                {
                    f = neighbour[i][2]; // Таким образом, сосед под номером j имеет наименьшее значение f функции F.
                    j = i;
                }
            }
        }
    }
    public void buildPath()
    {
        /*newPoints[0][0] = tick[0];
        newPoints[0][1] = tick[1]; // Добавляем стартовую точку (равна текущей в начале) в список ожидаемых точек. */
        i = 0;

        while((tick[0] != finish[0]) && (tick[1] != finish[1]))
        {
            getPoint();
            consideredPoints[i][2] = tick[0];
            consideredPoints[i][3] = tick[1];
            tick[0] = neighbour[j][0];
            tick[1] = neighbour[j][1];
            consideredPoints[i][0] = tick[0];
            consideredPoints[i][1] = tick[1];

            i = i + 1;

            if (i > m*n)
            {
                break;
                // зациклено (тупик, например)
            }
        }
    }
    public int getPath() {
    buildPath();

    /*int Path[][] = new int[i][2];
    for(j=0;j<i;j++)
    {
        Path[i][0] = consideredPoints[i][2];
        Path[i][1] = consideredPoints[i][3];
    }
    Path[j][0] = consideredPoints[i][0];
    Path[j][1] = consideredPoints[i][1];*/
        return (i+1); // Проверь эту хуйню
    }
}
