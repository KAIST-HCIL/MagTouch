package kaist.hcil.magtouchlibrary.datamodel;

public class Matrix2D {
    private final int row;
    private final int col;
    public final double[][] array;

    public Matrix2D(int row, int col)
    {
        this.row = row;
        this.col = col;
        array = new double[row][col];
    }

    public int getRow()
    {
        return row;
    }

    public int getCol()
    {
        return col;
    }

    public static Matrix2D mult(Matrix2D m1, Matrix2D m2){

        assert m1.col == m2.row;

        Matrix2D m = new Matrix2D(m1.row, m2.col);
        int K = m2.row;

        for (int i = 0; i<m1.row; i++)
        {
            for(int j = 0; j<m2.col; j++)
            {
                double sum = 0;
                for(int k = 0; k<K; k++)
                {
                    sum += (m1.array[i][k] * m2.array[k][j]);
                }
                m.array[i][j] = sum;
            }
        }
        return m;
    }

    public static Matrix2D transpose(Matrix2D m)
    {
        Matrix2D tm = new Matrix2D(m.col, m.row);

        for(int i = 0; i < m.row; i++)
        {
            for(int j = 0; j < m.col; j++)
            {
                tm.array[j][i] = m.array[i][j];
            }
        }
        return tm;
    }

    @Override
    public String toString()
    {
        String str = "";
        for(int i=0; i<row; i++)
        {
            for(int j=0; j<col; j++)
            {
                str += Double.toString(array[i][j]);
                if(j < col - 1 )
                {
                    str += ",";
                }
            }
            str += "\n";
        }
        return str;
    }
}
