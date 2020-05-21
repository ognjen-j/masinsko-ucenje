il    =    1;            % broj neurona u ulaznom sloju
ml    =   20;            % broj neurona u skrivenom sloju
ol    =    1;            % broj neurona u izlaznom sloju
g     =    2.000;        % brzina obucavanja
Emax  =    1e-10;        % maksimalna dozvoljena greska
ICmax = 5000;            % maksimalan broj iteracija   

tr_data = importdata('data/population.csv', ',', 3);
t_out = tr_data.data(:, 54) / 10^8; 
ttc = length(t_out);
t_in = linspace(-2, 2, ttc)';   

c = 0;
del = zeros(2, ml + 1);
kraj = 0;
w = ones(2, ml + 1, ml + 1) * 0.5;
w(1, 1 : ml, 1 : il + 1) = rand(ml, il + 1);
w(2, 1 : ol, 1 : ml + 1) = rand(ol, ml + 1);
a = g * 2;
w = -a + w .* 2 * a;
while (kraj == 0)
    E = 0;
    for t = 1 : ttc
        % Prostiranje unaprijed
        net = zeros(3, ml + 1, ml + 1);
        y = zeros(3, ml + 1, ml + 1);
        net(1, 1 : il) = t_in(t);
        net(1, il + 1) = 1;
        y(1, :) = net(1, :);
        for i = 1 : ml
            for j = 1 : il + 1
                net(2, i) = net(2, i) + w(1, i, j) * y(1, j);
            end
        end
        net(2, ml + 1) = 1;
        for i = 1 : ml
            y(2, i) = 1 / (1 + exp(-net(2, i)));
        end
        y(2, ml + 1) = 1;
        for i = 1 : ol
            for j = 1 : ml + 1
                net(3, i) = net(3, i) + w(2, i, j) * y(2, j);
            end
        end
        for i = 1 : ol
            y(3, i) = 1 / (1 + exp(-net(3, i)));
        end        
        d = t_out(t, :);
        Ep = 0;
        for i = 1 : ol
            Ep = Ep + 0.5 * (d(i) - y(3, i)) ^ 2;
        end
        E = E + Ep;
        if (Ep >= Emax)
            % Prostiranje greske unazad
            for i = 1 : ol
                del(2, i) = (d(i) - y(3, i)) * exp(-net(3, i)) / ((1 + exp(-net(3, i))) ^ 2);
            end
            for i = 1 : ol
                for j = 1 : ml + 1
                    dw = g * del(2, i) * y(2, j);
                    w(2, i, j) = w(2, i, j) + dw;
                end
            end
            for i = 1 : ml + 1
                del(1, i) = 0;
                for j = 1 : ol
                    del(1, i) = del(1, i) + w(2, j, i) * del(2, j);
                end
                del(1, i) = del(1, i) * exp(-net(2, i)) / ((1 + exp(-net(2, i))) ^ 2);
            end
            for i = 1 : ml
                for j = 1 : il + 1
                    dw = g * del(1, i) * y(1, j);
                    w(1, i, j) = w(1, i, j) + dw;
                end
            end
        end
    end
    c = c + 1;
    if ((E < Emax) || (c == ICmax))
        kraj = 1;
    end
end

t_in = 1960 : 2016;
y_out = zeros(1, ttc);

c = 1;
for itemp = 1960 : 0.1 : 2016
	net = zeros(3, ml + 1, ml + 1);
	y = zeros(3, ml + 1, ml + 1);
	net(1, 1 : il) = (itemp - 1960) / 14 - 2;
	net(1, il + 1) = 1;
	y(1, :) = net(1, :);
	for i = 1 : ml
		for j = 1 : il + 1
			net(2, i) = net(2, i) + w(1, i, j) * y(1, j);
		end
	end
	net(2, ml + 1) = 1;
	for i = 1 : ml
		y(2, i) = 1 / (1 + exp(-net(2, i)));
	end
	y(2, ml + 1) = 1;
	for i = 1 : ol
		for j = 1 : ml + 1
			net(3, i) = net(3, i) + w(2, i, j) * y(2, j);
		end
	end
	for i = 1 : ol
		y(3, i) = 1 / (1 + exp(-net(3, i)));
	end        
    y_out(c) = y(3, 1);
    c = c + 1;
end

plot(t_in, t_out, 'ob');
axis([1960 2016 0.73 0.83]);
grid on
hold on
plot(1960 : 0.1 : 2016, y_out, 'r', 'LineWidth', 2);
