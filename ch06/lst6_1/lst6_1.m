tt_data = importdata('data/squaresAndTriangles.csv', ',', 1);
X = tt_data.data(:, 1 : 2)';
Y = tt_data.data(:, 3);

% Velicina obucavajuceg skupa
P = length(Y);

% Rjesenje optimizacionog problema
% Tacnost
EPS = 1e-6;           
% Maksimalan broj iteracija
max_iter = 1000;
% Pocetne pretpostavke
change = ones(2 * P, 1);            
lambda = zeros(P, 1);
alpha = ones(P, 1);
t = 10000;

% Hesijan
H_FD = zeros(P, P);
for i = 1 : P
    for j = 1 : P
        H_FD(i, j) = Y(i) * Y(j) * X(:, i)' * X(:, j);
    end
end

% Brojac iteracija
iter = 0;

while ((norm(change, 2) > EPS) || (iter > max_iter))
    % Dualni rezidual
    r_dual = zeros(P, 1);
    for i = 1 : P
        FDp = 1;
        for j = 1 : P
            FDp = FDp - lambda(j) * Y(i) * Y(j) * X(:, i)' * X(:, j);
        end
        r_dual(i) = -FDp - alpha(i);
    end

    % Centralni rezidual
    r_cent = diag(alpha) * lambda - 1 / t * ones(P, 1);
    
    % Matrica sistema
    Ms = [H_FD -eye(P); diag(alpha) diag(lambda)];
    
    % Korekcija rjesenja
    change = -pinv(Ms) * [r_dual; r_cent];
    dlambda = change(1 : P);
    dalpha = change(P + 1 : 2 * P);
    
    % Korak korekcije
    da = dalpha(dalpha < 0);
    al = alpha(dalpha < 0);
    s = 0.99 * min([1; -al ./ da]);
    done = 0;
    while (done == 0)
        if (lambda + s * dlambda < 0)
            s = s * 0.8;
        else
            done = 1;
        end
    end
    
    % Konacna korekcija
    lambda = lambda + s * dlambda;
    alpha = alpha + s * dalpha;
    
    % Sljedeci korak
    iter = iter + 1;
end

% Vektor beta
beta = zeros(2, 1);
for i = 1 : P
    beta = beta + lambda(i) * Y(i) * X(:, i);
end

% Prvi lambda > 0
index = find(lambda > 0.001);
index = index(1);

% beta0
beta0 = 1 / Y(index) - X(:, index)' * beta;

% Prikaz
% y = k * x + n
k = - beta(1) / beta(2);
n = - beta0 / beta(2);
x = 0 : 0.01 : 1;
y = k * x + n;

plot(x, y, 'k', 'LineWidth', 1)
axis([0 1 0 1]);
hold on
for i = 1 : P
    if (Y(i) > 0)
        plot(X(1, i), X(2, i), 'bs', 'MarkerSize', 12);
    else
        plot(X(1, i), X(2, i), 'r>', 'MarkerSize', 12);
    end
end