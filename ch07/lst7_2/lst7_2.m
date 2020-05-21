full_data = importdata('data/iris.csv', ',', 1);
X = full_data.data(:, 1 : 4)';
[p, n] = size(X);

class_count = 3;
nc = [50 50 50];
muc = zeros(p, class_count);
j = 1;
for i = 1 : class_count
    muc(:, i) = mean(X(:, j : j + nc(i) - 1), 2);
    j = j + nc(i);
end
mu = mean(X, 2);

SB = zeros(p, p);
SW = zeros(p, p);
k = 0;
for i = 1 : class_count
    SB = SB + nc(i) * (muc(:, i) - mu) * (muc(:, i) - mu)';
    for j = 1 : nc(i)
        SW = SW + (X(:, k + j) - muc(:, i)) * (X(:, k + j) - muc(:, i))';
    end
    k = k + nc(i);
end

C = SW \ SB;
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

plot(Y(1, 1 : 50), zeros(1, 50), 'bo');
axis([-2 3 -1 1]);
hold on
plot(Y(1, 51 : 100), zeros(1, 50), 'rx');
plot(Y(1, 101 : 150), zeros(1, 50), 'gs');
