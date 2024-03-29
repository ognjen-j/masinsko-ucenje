podaci = importdata('data/population.csv', ',', 3);
y = podaci.data(:, 54);
n = length(y);
x = [ones(n, 1), linspace(0, 1, n)', linspace(0, 1, n).^2', linspace(0, 1, n).^3'];
plot(x(:, 2), y, 'x');
hold on;
teta = pinv(x'*x) * x' * y;
y_pred = polyval(fliplr(teta')', linspace(min(x(:, 2)), max(x(:, 2)), n));
plot(linspace(min(x(:, 2)), max(x(:, 2)), n), y_pred);
xticklabels({'1960', '', '', '', '', '2017'});
ylabel('Ukupna populacija');
xlabel('Godina');