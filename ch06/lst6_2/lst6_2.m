tt_data = importdata('data/iris1.csv', ',', 1);
X = tt_data.data(:, 3 : 4)';
Y = tt_data.data(:, 5) * 2 - 1;
% Klasa 1
X1 = X(:, Y > 0);
Y1 = Y(Y > 0);
% Klasa -1
X2 = X(:, Y < 0);
Y2 = Y(Y < 0);

% Velicina obucavajuceg skupa i klasa
P = length(Y);
P1 = length(Y1);
P2 = length(Y2);

% Minimalna udaljenost izmedju tacaka iz razlicitih klasa
mind = norm(X1(:, 1) - X2(:, 1), 2);
for i = 1 : P1
    for j = 1 : P2
        d = norm(X1(:, i) - X2(:, j), 2);
        if (d < mind)
            mind = d;
        end
    end
end
% Skupovi tacaka na toj udaljenosti (potpornih vektora)
mins1 = [];
mins2 = [];
for i = 1 : P1
    for j = 1 : P2
        d = norm(X1(:, i) - X2(:, j), 2);
        if (abs(d - mind) < 1e-6)
            if (isempty(mins1) || sum(ismember(mins1', X1(:, i)', 'rows')) == 0)
                mins1 = [mins1 X1(:, i)];
            end
            if (isempty(mins2) || sum(ismember(mins2', X2(:, j)', 'rows')) == 0)
                mins2 = [mins2 X2(:, j)];
            end
        end
    end
end
% Centri skupova
s1 = sum(mins1', 1)' / size(mins1, 2);
s2 = sum(mins2', 1)' / size(mins2, 2);
% Tacka na sredini duzi koja spaja s1 i s2
s = (s1 + s2) / 2;
% Vektor beta
beta = s1 - s2;
% Parametar beta0
beta0 = -beta' * s;

% Prikaz
% y = k * x + n
x = 0 : 0.01 : 7;
k = -beta(1) / beta(2);
n = -beta0 / beta(2);
y = k * x + n;

p1 = plot(x, y, 'k', 'LineWidth', 1);
axis([1 7 0 2.5]);
hold on
for i = 1 : P
    if (Y(i) > 0)
        p2 = plot(X(1, i), X(2, i), 'ob');
    else
        p3 = plot(X(1, i), X(2, i), 'xr');
    end
end