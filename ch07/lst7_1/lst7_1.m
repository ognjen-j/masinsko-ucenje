full_data = importdata('data/iris.csv', ',', 1);
X = full_data.data(:, 1 : 4)';
n = size(X, 2);
C = X * X' / (n - 1);
[U, S, V] = svd(C);
trS = trace(S);
k = 1;
sd = S(1, 1);
while (sd < 0.99 * trS)
    k = k + 1;
    sd = sd + S(k, k);
end
P = U(:, 1 : k)';
Y = P * X;

plot(Y(1, 1 : 50), Y(2, 1 : 50), 'bo');
axis([-12 -4 -3 3]);
hold on
plot(Y(1, 51 : 100), Y(2, 51 : 100), 'rx');
plot(Y(1, 101 : 150), Y(2, 101 : 150), 'gs');
